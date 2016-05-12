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

    private static final long THRESHOLD = 10000;
    private static final int NUM_FIELDS = 3;
    private static final int UNIQID = 0;
    private static final int INSTR  = 1;
    private static final int DEPTH  = 2;
    public static HashMap<Long, Long[]> factorizers = new HashMap<Long, Long[]>();

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

    // Updates the 
    public static synchronized void calcPrimeCall(int notUsed) {
        // Gets the fields for the current thread
        long threadID = Thread.currentThread().getId();
        Long[] fields = factorizers.get(threadID);

        // First time calling
        if (++fields[DEPTH] == 1) {
            logger.info("Starting new factorization on thread " + threadID);
        } else {
            logger.info("Found one factor on thread " + threadID);
        }
        factorizers.put(threadID, fields);
    }

    // When the factorization ends notifies send an update to Dynamo
    public static synchronized void calcPrimeReturn(int notUsed) {
        long threadID = Thread.currentThread().getId();
        Long[] fields = factorizers.get(threadID);
        // Last recursion?
        if (--fields[DEPTH] == 0) {
            logger.info("Stopping factorization on thread " + threadID);
            logger.info("Number of running factorizations: " + factorizers.size());
            // TODO Update Dynamo
            // HTTPServer.queue.add(fields[UNIQID].toString,
            //         threadID.toString,
            //         fields[INSTR],
            //         DynamoMessenger.INSCREMENT_INSTR);
        } else {
            factorizers.put(threadID, fields);
        }
    }

    // Keeps count of the number of ran instructions,
    // when they reach a threshold, send an update to Dynamo
    public static synchronized void basicBlockCount(int size) {
        long threadID = Thread.currentThread().getId();
        Long[] fields = factorizers.get(threadID);
        if (fields[INSTR] + size > THRESHOLD) {
            fields[INSTR] = (long) size;
            // TODO Update Dynamo
            // HTTPServer.queue.add(fields[UNIQID].toString,
            //         threadID.toString(),
            //         fields[INSTR],
            //         DynamoMessenger.INCREMENT_INSTR);
        } else {
            fields[INSTR] += size;
        }
        factorizers.put(threadID, fields);
    }

    // Initialize the unique-id (from DynamoDB) for this thread
    // so it can update the correct row later
    public static synchronized void insertUniqueId(String id) {
        long threadID = Thread.currentThread().getId();
        Long[] fields = new Long[NUM_FIELDS];

        fields[UNIQID] = Long.parseLong(id);
        fields[INSTR] = 0l;
        fields[DEPTH] = 0l;

        factorizers.put(threadID, fields);
    }
}
