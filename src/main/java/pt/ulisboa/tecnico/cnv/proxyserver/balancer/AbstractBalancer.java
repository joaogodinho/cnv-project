package pt.ulisboa.tecnico.cnv.proxyserver.balancer;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import pt.ulisboa.tecnico.cnv.proxyserver.DynamoConnecter;
import pt.ulisboa.tecnico.cnv.proxyserver.Instance;

public abstract class AbstractBalancer implements Balancer {
    final static Logger logger = Logger.getLogger(AbstractBalancer.class);

    protected ArrayList<Instance> workers;

    public AbstractBalancer() {
        logger.info("Initializing Balancer...");
        DynamoConnecter.createCrunchingTable();
        workers = new ArrayList<Instance>();
    }

    @Override
    public synchronized void notifyDeleteWorker(Instance instance) {
        logger.info("Removing worker " + instance.getId() + " from Balancer workers.");
        workers.remove(instance);
    }

    @Override
    public synchronized void notifyAddWorker(Instance instance) {
        logger.info("Added worker " + instance.getId() + " to Balancer workers.");
        workers.add(instance);
    }
}
