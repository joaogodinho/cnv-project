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
    private long lastStart;
    // CPU threshold, in percentage
    private final static int CPU_MAX_LOAD = 60;
    private final static int CPU_MIN_LOAD = 10;

    private Balancer balancer = null;
    private boolean running = true;
    private ArrayList<Instance> workers;

    // Only allows to launch a new worker when the previous
    // one is running. Serves as a control mechanism.
    private String lastWorker = null;

    private int num_req = 0;

    public synchronized void incNumReq() { num_req++; }
    public synchronized int getNumReq() {
        int temp = num_req;
        num_req = 0;
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
        if (lastWorker == null) {
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
        while (this.running) {
            logger.info("Checking workers status...");
            updateLastWorker();

            for (Instance i: workers) {
            }

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
            logger.info("Checking last worker status...");
            if (isInstanceHealthy(lastWorker)) {
                addWorker(lastWorker);
                lastWorker = null;
            }
        }
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
}
