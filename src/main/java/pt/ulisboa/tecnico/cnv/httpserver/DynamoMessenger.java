package pt.ulisboa.tecnico.cnv.httpserver;

import pt.ulisboa.tecnico.cnv.proxyserver.DynamoConnecter;

public class DynamoMessenger implements Runnable{


	public static final String INCREMENT_THREADS = "increment";
	public static final String DECREMENT_THREADS = "decrement";
	
	
	public DynamoMessenger() {
		System.out.println("created DynamoMessenger");
	}
	
	@Override
	public void run() {
		try {
			System.out.println("created DynamoMessenger");
			while(true){
				String message = HTTPServer.queue.take();  
				switch(message){
				case INCREMENT_THREADS : 
					DynamoConnecter.incrementThreadNumber("lel");
					break;
				case DECREMENT_THREADS :
					DynamoConnecter.decrementThreadNumber("lil");
					break;
				default: System.out.println("shit message yo!");
				}
				
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("server is exiting");
		}
	}


}
