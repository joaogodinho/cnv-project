package pt.ulisboa.tecnico.cnv.proxyserver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NumberCrunchingEntry{

    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

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
		try{
			Date startingTime = dateFormat.parse(this.startingTime);
			Date currentDate = new Date();
			long delta = startingTime.getTime() + getExpectedTime() - currentDate.getTime();
			return delta;
		}catch(Exception e){
			System.out.println("failed to parse date");
			//should never happen
			return 0;
		}
	}
	
	//TODO
	//Expected time of this instruction
	public static long getExpectedTime(){
		
		return 0;
	}
	
	
}
