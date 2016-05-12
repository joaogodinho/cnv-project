package pt.ulisboa.tecnico.cnv.httpserver;

import org.apache.log4j.Logger;

import pt.ulisboa.tecnico.cnv.proxyserver.DynamoConnecter;

public class DynamoMessenger implements Runnable{
    final static Logger logger = Logger.getLogger(DynamoMessenger.class);

	public static final String INCREMENT_THREADS = "increment";
	public static final String DECREMENT_THREADS = "decrement";
	public static final String INCREMENT_INSTRUCTIONS = "increment_instructions";
	public static final int UNIQUE_ID = 0;
	public static final int INSTRUCTION_COUNT = 1;
	public static final int TYPE_MESSAGE = 2;

	public DynamoMessenger() {
        logger.info("Created DynamoMessenger");
	}

	@Override
	public void run() {
		try {
			while(true){
				String message [] = HTTPServer.queue.take();
				switch(message[TYPE_MESSAGE]){
				case INCREMENT_THREADS :
					DynamoConnecter.incrementThreadNumber(HTTPServer.instanceID);
					break;
				case DECREMENT_THREADS :
					DynamoConnecter.decrementThreadNumber(HTTPServer.instanceID);
					break;
				case INCREMENT_INSTRUCTIONS :
					DynamoConnecter.incrementInstructions(message[UNIQUE_ID],message[INSTRUCTION_COUNT]);
					break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.info("Server is exiting");
		}
	}
}
