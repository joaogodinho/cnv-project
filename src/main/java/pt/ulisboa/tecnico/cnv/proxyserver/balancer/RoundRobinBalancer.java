package pt.ulisboa.tecnico.cnv.proxyserver.balancer;

import org.apache.log4j.Logger;

import pt.ulisboa.tecnico.cnv.proxyserver.Instance;

public class RoundRobinBalancer extends AbstractBalancer {
    final static Logger logger = Logger.getLogger(RoundRobinBalancer.class);

    private int index = 0;

    public RoundRobinBalancer() {
        super();
        logger.info("Initializing RounbRobinBalancer...");
    }

    public synchronized Instance requestInstance(String number) {
        logger.info("Got instance request for number = " + number);
        index = (index + 1)  % this.workers.size();
        return this.workers.get(index);
    }
}
