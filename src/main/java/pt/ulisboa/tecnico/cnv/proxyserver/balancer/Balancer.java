package pt.ulisboa.tecnico.cnv.proxyserver.balancer;

import pt.ulisboa.tecnico.cnv.proxyserver.Instance;

public interface Balancer {
    public void notifyDeleteWorker(Instance instance);
    public void notifyAddWorker(Instance instance);
    public String requestInstance(String number);
}
