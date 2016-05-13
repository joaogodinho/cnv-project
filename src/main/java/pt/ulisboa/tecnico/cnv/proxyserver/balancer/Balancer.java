package pt.ulisboa.tecnico.cnv.proxyserver.balancer;

import pt.ulisboa.tecnico.cnv.proxyserver.Instance;
import pt.ulisboa.tecnico.cnv.proxyserver.Scaler;

public interface Balancer {
    public void notifyDeleteWorker(Instance instance);
    public void notifyAddWorker(Instance instance);
    public Instance requestInstance(String number);
    public void setScaler(Scaler scaler);
}
