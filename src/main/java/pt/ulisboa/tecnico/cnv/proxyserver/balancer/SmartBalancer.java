package pt.ulisboa.tecnico.cnv.proxyserver.balancer;

import org.apache.log4j.Logger;

import java.lang.Math;
import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cnv.proxyserver.Instance;
import pt.ulisboa.tecnico.cnv.proxyserver.Scaler;
import pt.ulisboa.tecnico.cnv.proxyserver.NumberCrunchingEntry;

public class SmartBalancer extends AbstractBalancer {
    final static Logger logger = Logger.getLogger(SmartBalancer.class);

    private static final float REGR_ALPHA = 11.939f;
    private static final float REGR_BETA  = 0.6918f;
    private static final float REGR_DIV   = 2f;
    private static final float ALPHA      = 0.7f;
    private static final int BIT_LIM = 25;

    public SmartBalancer() {
        super();
        logger.info("Initializing SmartBalancer...");
    }

    public synchronized Instance requestInstance(String number) {
        //logger.info("Got instance request for number = " + number);
        List<Instance> workers = scaler.getWorkers();
        Instance targetWorker = workers.get(0);
        if (workers.size() > 1) {
            BigInteger numb = new BigInteger(number);
            int bits = numb.bitLength();
            if (bits > BIT_LIM) {
                ArrayList<Long> workersCost = new ArrayList<Long>();
                ArrayList<Integer> workersThreads = new ArrayList<Integer>();
                long expectedInst = predictNumbInst(bits);
                long largestCost = Long.MAX_VALUE;
                int largestThreads = Integer.MAX_VALUE;
                for (Instance i: workers) {
                    logger.info("Instance = " + i.getId() + " Cost = " + i.getThreadsCost() + " Threads = " + i.getNumberCurrentThreads());
                    if (i.getThreadsCost() < largestCost) { largestCost = i.getThreadsCost(); }
                    if (i.getNumberCurrentThreads() < largestThreads) { largestThreads = i.getNumberCurrentThreads(); }
                    workersCost.add(i.getThreadsCost());
                    workersThreads.add(i.getNumberCurrentThreads());
                }
                ArrayList<Float> finalCosts = new ArrayList<Float>();
                for (int i = 0; i < workers.size(); i++) {
                    float cost = (workersThreads.get(i) / (largestThreads+1)) * ALPHA + (workersCost.get(i) / (largestCost+1)) * (1 - ALPHA);
                    logger.info("Final Cost = " + cost);
                    finalCosts.add(cost);
                }
                float smallestCost = Float.MAX_VALUE;
                for (Float f: finalCosts) {
                    if (f < smallestCost) {
                        smallestCost = f;
                        targetWorker = workers.get(finalCosts.indexOf(f));
                    }
                }
            // Number not big enough, just send to the worker with less threads
            } else {
                for (Instance i: workers) {
                    if (i.getNumberCurrentThreads() < targetWorker.getNumberCurrentThreads()) {
                        targetWorker = i;
                    }
                }
            }
        }
        return targetWorker;
    }

    public static long predictNumbInst(int bits) {
       return Math.round(REGR_ALPHA * Math.exp(REGR_BETA * bits));
    }
}
