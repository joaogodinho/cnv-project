package pt.ulisboa.tecnico.cnv.proxyserver;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;

import pt.ulisboa.tecnico.cnv.proxyserver.AWS;
import pt.ulisboa.tecnico.cnv.proxyserver.balancer.Balancer;
import pt.ulisboa.tecnico.cnv.proxyserver.Instance;

import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.ec2.model.InstanceStatus;

public class Scaler extends Thread {
    final static Logger logger = Logger.getLogger(Scaler.class);

    private final static int SLEEP_TIME = 10000;
    // Cooldown (in milliseconds) after launching a new instance
    private final static int COOLDOWN = 2 * 60 * 1000;
    private long lastStart = 0;
    private final static int REQ = 0;
    private final static int BITS= 1;

    private Balancer balancer = null;
    private boolean running = true;
    private ArrayList<Instance> workers;

    // Variables for balancing/scaling
    private final static float EPSILON = 0.02f;
    private final static int BIT_THRESHOLD = 30;
    private final static float REQ_THRESHOLD = 1.6f;
    private final static float REQ_ALPHA = 0.4f;
    private final static float REQ_BETA = 0.8f;
    private final static int REQ_MAX_PERIOD = 3;
    private final static int MAX_THREADS_WORKER = 20;
    private final static float THREADS_MAX_RATIO = 0.8f;
    private final static float REQ_MAX_RATIO = 0.9f;
    private final static float BIT_MAX_RATIO = 1.0f;
    private final static float REQ_WEIGHT = 0.3f;
    private final static float BIT_WEIGHT = 0.45f;
    private final static float THREAD_WEIGHT = 0.25f;
    private final static float SYSTEM_THRESHOLD = 0.55f;

    // Only allows to launch a new worker when the previous
    // one is running. Serves as a control mechanism.
    private String lastWorker = null;

    // Used to calculate the number of requests/sec and
    // average number of bits
    private int num_req = 0;
    private int total_bits = 0;

    public synchronized void incReq(int num_bits) {
        num_req++;
        total_bits += num_bits;
    }

    // Returns an array with number of requests
    // and total number of bits. Resets the counters
    public synchronized int[] getReqMetrics() {
        int[] temp = { num_req, total_bits };
        num_req = 0;
        total_bits = 0;
        return temp;
    }

    /**
     *  Default constructor, initializaes
     *  AWS and starts an instance
     */
    public Scaler() throws Exception {
        logger.info("Initializing Scaler...");
        AWS.init();
        workers = new ArrayList<Instance>();
        // Launch one worker
        lastWorker = "i-a277141e";
        startWorker();
    }

    /**
     * Takes a balancer as argument to be able
     * to notify it when a new instance is running
     */
    public Scaler(Balancer balancer) throws Exception {
        this();
        this.balancer = balancer;
    }

    public synchronized void stopRunning() { this.running = false; }

    /**
     * Starts a new worker only if there's no worker
     * warming up.
     * Returns a new instance
     */
    private String startWorker() {
        if (lastWorker == null && lastStart + COOLDOWN < new Date().getTime()) {
            logger.info("Starting worker...");
            lastWorker = AWS.createInstance();
            logger.info("Started worker with ID = " + lastWorker);
            lastStart = new Date().getTime();
        } else {
            logger.info("Last started worker is still warming up, not starting a new one.");
        }
        return lastWorker;
    }

    /**
     * Stops the worker with the given ID
     * and notifies the balancer that it's no
     * longer available
     */
    private void stopWorker(Instance instance) {
        logger.info("Stopping worker with id = " + instance.getId());
        logger.info("Notifying Balancer...");
        balancer.notifyDeleteWorker(instance);
        AWS.terminateInstance(instance.getId());
        workers.remove(instance);
    }

    /**
     * Main logic, runs until stopRunning is
     * called.
     * TODO Check the workers load, if needed start
     * new worker
     */
    public void run() {
        logger.info("Running Scaler thread...");
        // Number of times the requests are over the limit
        int reqPeriods = 0;
        int bitPeriods = 0;
        int threadsPeriods = 0;
        while (this.running) {
            int[] reqs = getReqMetrics();
            if (reqs[REQ] != 0 && reqs[BITS] != 0 && workers.size() != 0) {
                float systemLoad = 0.0f;
                float reqsec = (float) reqs[REQ] / (SLEEP_TIME / 1000);
                float bitavg = (float) reqs[BITS] / reqs[REQ];
                float threadavg = 0.0f;

                for (Instance i: workers) {
                    threadavg += i.getNumberCurrentThreads();
                }
                threadavg /= workers.size();

                logger.info("Average req/sec = " + reqsec);
                logger.info("Average bits = " + bitavg);
                logger.info("Average threads = " + threadavg);

                float reqsecratio = Math.min(calcReqSecLoad(reqsec, workers.size()), 1.0f);
                float bitratio = Math.min(calcBitLoad(bitavg, workers.size()), 1.0f);
                float threadratio = Math.min(calcThreadLoad(threadavg, workers.size()), 1.0f);

                logger.info("req/sec ratio = " + reqsecratio);
                logger.info("bit ratio = " + bitratio);
                logger.info("thread ratio = " + threadratio);

                if (reqsecratio >= REQ_MAX_RATIO) {
                    if (++reqPeriods >= 3) {
                        logger.info("Trigger from requests ratio.");
                        systemLoad += reqsecratio * REQ_WEIGHT;
                    }
                } else { reqPeriods = 0; }

                if (bitratio >= BIT_MAX_RATIO) {
                    if (++bitPeriods >= 3) {
                        logger.info("Trigger from bit ratio.");
                        systemLoad += bitratio * BIT_WEIGHT;
                    }
                } else { bitratio = 0; }

                if (threadratio >= THREADS_MAX_RATIO) {
                    if (++threadsPeriods >= 3) {
                        logger.info("Trigger from threads ratio.");
                        systemLoad += threadratio * THREAD_WEIGHT;
                    }
                } else { threadsPeriods = 0; }

                systemLoad /= workers.size();
                logger.info("System total load = " + systemLoad);
                if (systemLoad >= SYSTEM_THRESHOLD) {
                    logger.info("System overload, start new worker.");
                    reqPeriods = 0; bitPeriods = 0; threadsPeriods = 0;
                    startWorker();
                }
            }

            logger.info("Checking workers status...");
            updateLastWorker();


            try { Thread.sleep(SLEEP_TIME); } catch (Exception e) { }
        }
        terminate();
        logger.info("Finishing Scaler thread.");
    }

    /**
     * Stops all workers, called when shutting
     * down.
     */
    private void terminate() {
        logger.info("Terminating Scaler...");
        for (Instance instance: workers) {
            AWS.terminateInstance(instance.getId());
            balancer.notifyDeleteWorker(instance);
        }
        // In case an instance is launching when we terminate
        if (lastWorker != null) { AWS.terminateInstance(lastWorker); }
    }

    /**
     * Checks if the last started worker status,
     * allowing to start a new one if previous is running.
     */
    private void updateLastWorker() {
        if (lastWorker != null) {
            if (isInstanceHealthy(lastWorker)) {
                addWorker(lastWorker);
                lastWorker = null;
            }
        }
        if (workers.size() == 1) { startWorker(); }
    }

    public synchronized List<Instance> getWorkers() {
        return Collections.unmodifiableList(workers);
    }

    // Checks if the instance with the given ID is
    // healthy
    private boolean isInstanceHealthy(String instanceId) {
        InstanceStatus is = AWS.getInstanceStatus(lastWorker);
        if (is != null) {
            if (is.getInstanceState().getName().equals(AWS.INST_RUNNING) &&
                    is.getInstanceStatus().getStatus().equals(AWS.INST_OK) &&
                    is.getSystemStatus().getStatus().equals(AWS.INST_OK)) {
                return true;
            }
        }
        return false;
    }

    // Adds the instance with the given ID to the workers
    // and notifies Balancer
    private void addWorker(String instanceId) {
        Instance ins = AWS.getInstance(lastWorker);
        logger.info("Last worker is running. Adding " + lastWorker + " to workers pool.");
        workers.add(ins);
        logger.info("Notifying Balancer...");
        balancer.notifyAddWorker(ins);
    }

    private float calcReqSecLoad(float reqsec, int numbWorkers) {
        return (reqsec / numbWorkers) / REQ_THRESHOLD;
    }

    private float calcBitLoad(float avgbit, int numbWorkers) {
        return (avgbit / numbWorkers) / BIT_THRESHOLD;
    }

    private float calcThreadLoad(float avgthreads, int numbWorkers) {
        return (avgthreads / numbWorkers) / MAX_THREADS_WORKER;
    }
}
