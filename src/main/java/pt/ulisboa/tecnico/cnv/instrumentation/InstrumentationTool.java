package pt.ulisboa.tecnico.cnv.instrumentation;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import java.util.HashMap;
import java.lang.Thread;

import BIT.highBIT.BasicBlock;
import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;
import pt.ulisboa.tecnico.cnv.httpserver.DynamoMessenger;
import pt.ulisboa.tecnico.cnv.httpserver.HTTPServer;

import org.apache.log4j.Logger;

public class InstrumentationTool {
    final static Logger logger = Logger.getLogger(InstrumentationTool.class);

    private static final String itPackage = "pt/ulisboa/tecnico/cnv/instrumentation/InstrumentationTool";
    private static long instCount = 0;
    private static int depth = 0;
    // private static final int NUM_FIELDS = 2;
    // private static final int INSTR = 0;
    // private static final int RECUR = 1;
    // public static HashMap<Long, Long[]> factorizers = new HashMap<Long, Long[]>();

    public static final String usage = "Usage: java InstrumentationTool input_class"
        + "\nThis instrumentation logs the number of threads, recursion depth and instruction count."
        + "\nLogging is written to log4j-metrics.log";

	public static void main(String args[]) {
        try {
			if (args.length < 1) {
				System.err.println(usage);
				System.exit(-1);
			}
			File file_in = new File(args[0]);
			String path = new String(file_in.getAbsolutePath());
            assert path.endsWith(".class");
            instrument(path);
		} catch (Exception e) {
			System.err.println("Exception ocurred, check log for details.");
            e.printStackTrace();
            logger.fatal("Exception in main:");
            logger.fatal(e.getMessage());
			System.exit(-1);
		}
	}

    /**
     *  Instruments the given class file.
     *  Class should be of type IntFactorization. Adds calls before and after callPrimeFactors
     *  and before every basic block.
     */
    @SuppressWarnings("unchecked")
    public static void instrument(String classFile) {
        try {
            ClassInfo ci = new ClassInfo(classFile); /* read & process the class */

			Vector<Routine> routines = ci.getRoutines();

            for (Routine routine: routines) {
                if (routine.getMethodName().equals("calcPrimeFactors")) {
                    routine.addBefore(itPackage, "calcPrimeCall", 0);
			        for (Enumeration<BasicBlock> bb = routine.getBasicBlocks().elements();bb.hasMoreElements();){
    		        	BasicBlock b = bb.nextElement();
			        	b.addBefore(itPackage, "basicBlockCount", b.size());
                    }
                    routine.addAfter(itPackage, "calcPrimeReturn", 0);
                }
			}
			ci.write(classFile);
        } catch (Exception e) {
            System.err.println("Exception ocurred, check log for details.");
            e.printStackTrace();
            logger.fatal("Exception in instrumentThreadCount:");
            logger.fatal(e.getMessage());
        }
    }

    /**
     * Creates or updates HashMap <K,V> when calcPrimeFactors is called.
     * Creating  sets recursion depth at 1 and instructions at 0.
     * Updating increments recursion depth
     */
    public static synchronized void calcPrimeCall(int notUsed) {
        // long threadID = Thread.currentThread().getId();
        // // Existing thread
        // if (factorizers.containsKey(threadID)) {
        //     Long[] fields = factorizers.get(threadID);
        //     fields[RECUR]++;
        //     factorizers.put(threadID, fields);
        //     logger.info("TID=" + threadID + " DEPTH:" + fields[RECUR] + " ++");
        // // New thread
        // } else {
        //     Long[] fields = new Long[NUM_FIELDS];
        //     fields[INSTR] = 0L;
        //     fields[RECUR] = 1L;
        //     factorizers.put(threadID, fields);
        //     logger.info("NUM_THREADS: " + factorizers.size());
        //     logger.info("STARTING TID:" + threadID);
        //     HTTPServer.queue.add(DynamoMessenger.INCREMENT_THREADS);
        // }
        logger.info("CalcPrime call");
        depth++;
    }

    /**
     * Removes or updates HashMap <K,V> when calcPrimeFactors returns.
     * Removing deletes value from HashMap
     * Updating decrements recursion depth
     */
    public static synchronized void calcPrimeReturn(int notUsed) {
        // long threadID = Thread.currentThread().getId();
        // Long[] fields = factorizers.get(threadID);
        // // Last recursion?
        // if (--fields[RECUR] == 0) {
        //     factorizers.remove(threadID);
        //     logger.info("ENDING TID:" + threadID + " NUM_INSTR:" + fields[INSTR]);
        //     logger.info("NUM_THREADS: " + factorizers.size());
        //     HTTPServer.queue.add(DynamoMessenger.DECREMENT_THREADS);
        // } else {
        //     factorizers.put(threadID, fields);
        //     logger.info("TID=" + threadID + " DEPTH:" + fields[RECUR] + " --");
        // }
        if (--depth == 0) {
            logger.info("INSTRUCTIONS COUNT: " + instCount);
            instCount = 0;
        }
    }

    /**
     * Increments number of instructions by given basic block size.
     */
    public static synchronized void basicBlockCount(int size) {
        instCount += size;
        // long threadID = Thread.currentThread().getId();
        // Long[] fields = factorizers.get(threadID);
        // fields[INSTR] += size;
        // factorizers.put(threadID, fields);
    }
}
