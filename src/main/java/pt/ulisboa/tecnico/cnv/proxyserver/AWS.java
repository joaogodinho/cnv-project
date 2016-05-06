package pt.ulisboa.tecnico.cnv.balancerserver;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public final class AWS {
    // AWS EC2 Endpoint (Frankfurt)
    private static final String EC2_ENDPOINT = "ec2.eu-central-1.amazonaws.com";
    // Amazon Linux AMI 2016.03.0
    // TODO Change to ID with running server (workers)
    private static final String IMAGE_ID = "ami-e2df388d";

    // Instance type to be used, should be final
    private static final String INST_TYPE = "t2.micro";

    // Min and Max number of instances to launch, at a time
    private static final int INST_COUNT = 1;

    private static final String KEY_NAME = "CNV-Project";

    // Default Security Group, allow all inbound from current IP, all outbound
    // USE NAME, NOT ID
    // TODO Change to group for workers
    private static final String SEC_GROUP = "default";

    private static AmazonEC2 ec2 = null;
    private static RunInstancesRequest runInstanceReq = null;

    private AWS() { }

    public static void init() throws Exception {
        if (ec2 == null || runInstanceReq == null) {
            AWSCredentials credentials = null;
            try {
                credentials = new ProfileCredentialsProvider().getCredentials();
            } catch (Exception e) {
                throw new AmazonClientException(
                        "Cannot load the credentials from the credential profiles file. " +
                        "Please make sure that your credentials file is at the correct " +
                        "location (~/.aws/credentials), and is in valid format.",
                        e);
            }
            ec2 = new AmazonEC2Client(credentials);
            ec2.setEndpoint(EC2_ENDPOINT);

            runInstanceReq = new RunInstancesRequest();
            runInstanceReq.withImageId(IMAGE_ID)
                .withInstanceType(INST_TYPE)
                .withMinCount(INST_COUNT)
                .withMaxCount(INST_COUNT)
                .withKeyName(KEY_NAME)
                .withSecurityGroups(SEC_GROUP);
        }
    }

    // Creates a new instance and returns the instance ID
    public static String createInstance() {
        RunInstancesResult instanceResult = ec2.runInstances(runInstanceReq);
        return instanceResult.getReservation().getInstances().get(0).getInstanceId();
    }

    // Terminates the instance with the given ID
    public static void terminateInstance(String instanceId) {
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(instanceId);
        ec2.terminateInstances(termInstanceReq);
    }

    public static void getInstances() { }
}