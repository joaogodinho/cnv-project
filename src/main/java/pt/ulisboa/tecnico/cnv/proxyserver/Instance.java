package pt.ulisboa.tecnico.cnv.proxyserver;

public class Instance {
    private String instanceId;
    private String instanceDns;
    private int instanceStatus;

    public Instance() { }

    public Instance(int status, String id, String dns) {
        instanceStatus = status;
        instanceId = id;
        instanceDns = dns;
    }

    public int getStatus() { return instanceStatus; }

    public String getId() { return instanceId; }

    public String getDns() { return instanceDns; }

    public void setStatus(int status) { instanceStatus = status; }

    public void setId(String id) { instanceId = id; }

    public void setDns(String dns) { instanceDns = dns; }

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
}
