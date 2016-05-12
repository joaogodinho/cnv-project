package pt.ulisboa.tecnico.cnv.proxyserver.balancer;

import org.apache.log4j.Logger;

import java.lang.Math;

import pt.ulisboa.tecnico.cnv.proxyserver.Instance;

public class SmartBalancer extends AbstractBalancer {
    final static Logger logger = Logger.getLogger(SmartBalancer.class);

    private static final float REGR_ALPHA = 14.433f;
    private static final float REGR_BETA  = 0.6985f;
    private static final float REGR_DIV   = 2f;

    public SmartBalancer() {
        super();
        logger.info("Initializing SmartBalancer...");
    }

    public synchronized Instance requestInstance(String number) {
        logger.info("Got instance request for number = " + number);
        return null;
    }

    private static long predictNumbInst(int bits) {
       return Math.round(REGR_ALPHA * Math.exp(REGR_BETA * bits / REGR_DIV));
    }
}
