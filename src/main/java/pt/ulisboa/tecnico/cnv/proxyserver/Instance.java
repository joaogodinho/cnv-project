package pt.ulisboa.tecnico.cnv.proxyserver;

import java.util.concurrent.LinkedBlockingQueue;

public class Instance{
    private String instanceId;
    private String instanceDns;
    private int instanceStatus;

    
    private LinkedBlockingQueue<NumberCrunchingEntry> threads = new LinkedBlockingQueue<NumberCrunchingEntry>();
    
    // Values for Exponential Moving Average
    // -1 notifies first time incrementing average
    private double cpuEMA = -1;
    private static final double ALPHA = 0.5;

    public Instance() { }

    public Instance(int status, String id, String dns) {
        instanceStatus = status;
        instanceId = id;
        instanceDns = dns;
    }
    
    public boolean insertTask(NumberCrunchingEntry entry){
    	return this.threads.add(entry);
    }
    
    public int getNumberCurrentThreads(){
		return this.threads.size();
	}

    public int getStatus() { return instanceStatus; }

    public String getId() { return instanceId; }

    public String getDns() { return instanceDns; }

    public double getCpuEMA() { return cpuEMA; }

    public void setStatus(int status) { instanceStatus = status; }

    public void setId(String id) { instanceId = id; }

    public void setDns(String dns) { instanceDns = dns; }

    public void incCpuEMA(double load) {
        if (cpuEMA == -1) { cpuEMA = load; }
        else { cpuEMA = ALPHA * load + (1 - ALPHA) * cpuEMA; }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Instance) {
            Instance ins = (Instance) obj;
            if (ins.getId().equals(this.getId())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

	public void removeTask(NumberCrunchingEntry entry) {
		this.threads.remove(entry);
	}
	
	private long getLowestCostThread(){
		long cost = Long.MAX_VALUE; 
		for(NumberCrunchingEntry c : this.threads){
			long currentCost = c.getCurrentCost();
			if(currentCost < cost) 
				cost = currentCost;
		}
		return cost;
	}

	public Instance compareTo(Instance o) {
		return this.getLowestCostThread() < o.getLowestCostThread() ? this : o;
	}

}
