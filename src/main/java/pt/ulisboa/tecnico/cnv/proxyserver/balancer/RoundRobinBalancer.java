package pt.ulisboa.tecnico.cnv.proxyserver.balancer;

import org.apache.log4j.Logger;

public class RoundRobinBalancer extends AbstractBalancer {
    final static Logger logger = Logger.getLogger(RoundRobinBalancer.class);

    private int index = 0;

    public RoundRobinBalancer() {
        super();
        logger.info("Initializing RounbRobinBalancer...");
    }

    public synchronized String requestInstance(String number) {
        logger.info("Got instance request for number = " + number);
        index = (index + 1)  % this.workers.size();
        return this.workers.get(index).getDns();
    }
}
