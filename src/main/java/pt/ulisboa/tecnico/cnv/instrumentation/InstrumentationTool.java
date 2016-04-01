package pt.ulisboa.tecnico.cnv.instrumentation;

import java.io.File;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Vector;

import BIT.highBIT.BasicBlock;
import BIT.highBIT.ClassInfo;
import BIT.highBIT.Instruction;
import BIT.highBIT.InstructionTable;
import BIT.highBIT.Routine;
import BIT.lowBIT.CONSTANT_Class_Info;
import BIT.lowBIT.CONSTANT_Utf8_Info;
import BIT.lowBIT.ClassFile;
import BIT.lowBIT.Cp_Info;
import BIT.lowBIT.Method_Info;

import org.apache.log4j.Logger;

public class InstrumentationTool {
    final static Logger logger = Logger.getLogger(InstrumentationTool.class);

    public static int threadCount = 0;

	public static int numberOfInstructions = 0;
	public static int numberOfConditionChecks = 0;
	public static double grand_total = 0;
    public static final String usage = "Usage: java InstrumentationTool input_class"
        + "\nThis instrumentation counts the number of instructions and conditional checks on runtime."
        + " The original class is replaced.";

    private static final String itPackage = "pt/ulisboa/tecnico/cnv/instrumentation/InstrumentationTool";

	public static void main(String args[]) {
        try {
			if (args.length < 1) {
				System.err.println(usage);
				System.exit(-1);
			}
			File file_in = new File(args[0]);
			String path = new String(file_in.getAbsolutePath());
            assert path.endsWith(".class");
            //processClass(path);
			// processFiles(class_in);
            instrumentThreadCount(path);
		} catch (Exception e) {
			System.err.println("Exception ocurred, check log for details.");
            e.printStackTrace();
            logger.fatal("Exception in main:");
            logger.fatal(e.getMessage());
			System.exit(-1);
		}
	}

    @SuppressWarnings("unchecked")
    public static void instrumentThreadCount(String classFile) {
        try {
			ClassInfo ci = new ClassInfo(classFile); /* read & process the class */

			Vector<Routine> routines = ci.getRoutines();

            for (Routine routine: routines) {
                if (routine.getMethodName().equals("callFactorize")) {
                    routine.addBefore(itPackage, "incThreads", 0);
                    routine.addAfter(itPackage, "decThreads", 0);
                    
                    routine.addBefore(itPackage, "printThreadCount", 0);
                    routine.addAfter(itPackage, "printThreadCount", 0);
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

    @SuppressWarnings("unchecked")
    public static void processClass(String classFile) {
        try {
            /* BIT/highBIT/ClassInfo */
			ClassInfo ci = new ClassInfo(classFile); /* read & process the class */

			/* BIT/lowBIT/ClassFile */
			ClassFile cf = ci.getClassFile(); /* returns the class file in the BIT representation*/
			short super_class_index = cf.getSuperClassIndex();

            /* Type Cp_Info can be found in BIT/lowBIT/Cp_Info */
			Cp_Info[] cpool = ci.getConstantPool(); /* ci is type BIT/highBIT/ClassInfo */

			/*
			 * get the element in the constant pool at super_class_index
			 * its type is BIT/lowBIT/CONSTANT_Class_Info
			 */
			CONSTANT_Class_Info tmp_class_info = (CONSTANT_Class_Info) cpool[super_class_index];

			/* get the name index from the CONSTANT_Class_Info element */
			int name_index = tmp_class_info.name_index;

			/*
			 * get the element in the constant pool at name_index
			 * its type is BIT/lowBIT/CONSTANT_Utf8_Info
			 */
			CONSTANT_Utf8_Info tmp_utf8_info = (CONSTANT_Utf8_Info) cpool[name_index];

			/* convert the utf8 object to a string */
			String supername = new String(tmp_utf8_info.bytes);

			Vector<Routine> routines = ci.getRoutines();
            /* BIT/highBIT/ClassInfo call that returns a vector
    		 * containing all of the methods in the class */

            for (Routine routine: routines) {
			// for (Enumeration e=routines.elements(); e.hasMoreElements();) {
    		// 	Routine routine = (Routine) e.nextElement();

                /* nextElement returns type Object
				 * hence, we need the cast */
                /* see BIT/highBIT/Routine for all the ways to manipulate a routine */
				System.err.println("class: " + routine.getClassName() + " method: "
					+ routine.getMethodName()
					+ " type: " + routine.getDescriptor());

				/* see BIT/lowBIT/Method_Info */
				Method_Info meth = routine.getMethodInfo();
				System.out.println(routine.getInstructionCount());

			    for(Enumeration<BasicBlock> bb = routine.getBasicBlocks().elements();bb.hasMoreElements();){
    				BasicBlock b = bb.nextElement();
					b.addBefore("pt/ulisboa/tecnico/cnv/instrumentation/InstrumentationTool", "count", b.size());
				}
				for(Instruction instruction : routine.getInstructions()){
				    if(InstructionTable.InstructionTypeTable[instruction.getOpcode()] == InstructionTable.CONDITIONAL_INSTRUCTION)
    					instruction.addBefore("pt/ulisboa/tecnico/cnv/instrumentation/InstrumentationTool", "countConditions", 1);
				}
				// if(routine.getMethodName().equals("calcPrimeFactors"))
				// 	routine.addAfter("pt/ulisboa/tecnico/cnv/instrumentation/InstrumentationTool","printICount", "nothing")nb;
                if (routine.getMethodName().equals("callFactorize")) {
				 	routine.addAfter("pt/ulisboa/tecnico/cnv/instrumentation/InstrumentationTool","printICount", "nothing");
                    routine.addAfter("pt/ulisboa/tecnico/cnv/instrumentation/InstrumentationTool", "resetCounters", "nothing");
                }
			}
			ci.write(classFile);
        } catch (Exception e) {
            System.err.println("Exception ocurred, check log for details.");
            e.printStackTrace();
            logger.fatal("Exception in processClass:");
            logger.fatal(e.getMessage());
        }
    }

 	public static synchronized void printICount(String foo) {
        logger.info("prime was calculated in " + numberOfInstructions + " instructions ");
        logger.info("prime was calculated with " + numberOfConditionChecks + " conditions ");
    }

 	public static synchronized void countConditions(int incr){
 		numberOfConditionChecks +=incr;
 	}

    public static synchronized void resetCounters(String foo) {
        numberOfInstructions = 0;
        numberOfConditionChecks = 0;
    }

    public static synchronized void count(int incr) {
    	numberOfInstructions += incr;
    }

    public static synchronized void incThreads(int notUsed) {
        threadCount++;
    }

    public static synchronized void decThreads(int notUsed) {
        threadCount--;
    }

    public static synchronized void printThreadCount(int notUsed) {
        logger.info("#Threads=" + threadCount);
    }
}
