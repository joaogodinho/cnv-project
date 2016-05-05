package pt.ulisboa.tecnico.cnv.proxyserver;

import pt.ulisboa.tecnico.cnv.proxyserver.Balancer;

public class BalancerRoundRobin extends Balancer {
    private int index = 0;

    @Override
    public synchronized String requestInstance(String number) {
        index = (index + 1) % workers.size();
        return workers.get(index);
    }
}
