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

public class Scaler extends Thread {
    final static Logger logger = Logger.getLogger(Scaler.class);

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
        int sleepMilliSeconds = 10000;
        int metricsTriggerTime = 60 * 1000;
        int minuteCounter = 0;
        while (this.running) {
            logger.info("Checking workers status...");
            // Trigger metrics logging every minute
            if (minuteCounter % metricsTriggerTime == 0) {
                logger.info("Fetching workers metrics...");
                updateCPULoad();
                checkCPULoad();
                minuteCounter = 0;
            }

            updateLastWorker();

            minuteCounter += sleepMilliSeconds;
            try { Thread.sleep(sleepMilliSeconds); } catch (Exception e) { }
        }
        terminate();
        logger.info("Finishing Scaler thread.");
    }

    private void updateCPULoad() {
        for (Instance instance: workers) {
            List<Datapoint> datapoints = AWS.getAvgCPU(instance);
            if (datapoints != null && datapoints.size() > 0) {
                logger.info("Updating load average for " + instance.getId());
                // Always get last datapoint and add it to the instance CPU EMA
                double load = datapoints.get(datapoints.size() - 1).getAverage();
                instance.incCpuEMA(load);
            }
        }
    }

    private void checkCPULoad() {
        logger.info("Checking instances CPU load...");
        double systemAvg = 0;
        int counter = 0;
        boolean overload = false;
        for (Instance instance: workers) {
            if (instance.getCpuEMA() != -1) {
                logger.info("CPU Average for " + instance.getId() + " is = " + instance.getCpuEMA());
                systemAvg += instance.getCpuEMA();
                counter++;
                if (instance.getCpuEMA() > CPU_MAX_LOAD) { overload = true; }
            }
        }
        logger.info("System Average = " + systemAvg / counter);
        if (overload && lastStart + COOLDOWN > new Date().getTime()) {
            logger.info("At least one worker is working too much, spawning new one...");
            startWorker();
        }
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
            Instance instance = AWS.getInstance(lastWorker);
            if (instance.getStatus() == AWS.INST_RUNNING) {
                logger.info("Last worker is running.");
                logger.info("Adding " + lastWorker + " to workers pool.");
                workers.add(instance);
                logger.info("Notifying Balancer...");
                balancer.notifyAddWorker(instance);
                lastWorker = null;
            } else {
                logger.info("Last worker not running, status = " + instance.getStatus());
            }
        }
    }

    public synchronized List<Instance> getWorkers() {
        return Collections.unmodifiableList(workers);
    }
}
