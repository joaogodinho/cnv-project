package pt.ulisboa.tecnico.cnv.proxyserver;

import java.util.ArrayList;

import org.apache.log4j.Logger;


public class Balancer {
    final static Logger logger = Logger.getLogger(Balancer.class);

    protected ArrayList<String> workers;

    public Balancer() {
        logger.info("Initializing Balancer...");
        workers = new ArrayList<String>();
    }

    public synchronized void notifyDeleteWorker(String instanceId) {
        logger.info("Removing worker " + instanceId + " from Balancer workers.");
        workers.remove(instanceId);
    }

    public synchronized void notifyAddWorker(String instanceId) {
        logger.info("Adding worker " + instanceId + " to Balancer workers.");
        workers.add(instanceId);
    }

    /**
     * Each implementation of Balancer should override this
     */
    public synchronized String requestInstance(String number) {
        return null;
    }
}
