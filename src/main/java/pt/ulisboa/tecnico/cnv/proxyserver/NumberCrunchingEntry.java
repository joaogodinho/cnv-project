package pt.ulisboa.tecnico.cnv.proxyserver;

public class NumberCrunchingEntry{


	private Integer id;
	private int numberBits;
	private String startingTime;
	
	public NumberCrunchingEntry(int id, String startingTime, int number_bits){
		this.id= id;
		this.startingTime = startingTime;
		this.numberBits = number_bits;
	}
	
	public Integer getID(){
		return id;
	}
	
	public int getNumberBits(){
		return numberBits;
	}
	
	public String getStartingTime(){
		return startingTime;
	}

	public long getCurrentCost(){
		long delta = getExpectedInstructions(numberBits) - DynamoConnecter.getNumberOfInstructions(String.valueOf(this.id));
		return delta;
	}
	
	//TODO
	//Expected time of this instruction
	public static long getExpectedInstructions(int number_bits){
		
		return 0;
	}
	
	
}
