package pt.ulisboa.tecnico.cnv.httpserver;

import org.apache.log4j.Logger;

import pt.ulisboa.tecnico.cnv.proxyserver.DynamoConnecter;

public class DynamoMessenger implements Runnable{
    final static Logger logger = Logger.getLogger(DynamoMessenger.class);

	public static final String INCREMENT_THREADS = "increment";
	public static final String DECREMENT_THREADS = "decrement";


	public DynamoMessenger() {
        logger.info("Created DynamoMessenger");
	}

	@Override
	public void run() {
		try {
			System.out.println("created DynamoMessenger");
			while(true){
				String message = HTTPServer.queue.take();
				switch(message){
				case INCREMENT_THREADS :
					DynamoConnecter.incrementThreadNumber(HTTPServer.instanceID);
					break;
				case DECREMENT_THREADS :
					DynamoConnecter.decrementThreadNumber(HTTPServer.instanceID);
					break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.info("Server is exiting");
		}
	}
}
