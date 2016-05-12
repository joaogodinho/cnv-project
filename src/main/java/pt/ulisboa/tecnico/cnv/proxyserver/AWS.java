package pt.ulisboa.tecnico.cnv.proxyserver;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStatus;

import org.apache.log4j.Logger;

import pt.ulisboa.tecnico.cnv.proxyserver.Instance;

public final class AWS {
    final static Logger logger = Logger.getLogger(AWS.class);
    // AWS EC2 Endpoint (Frankfurt)
    private static final String EC2_ENDPOINT = "ec2.eu-central-1.amazonaws.com";
    private static final String CW_ENDPOINT = "monitoring.eu-central-1.amazonaws.com";
    // Amazon Linux AMI 2016.03.0
    // TODO Change to ID with running server (workers)
    private static final String IMAGE_ID = "ami-e47d908b";

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

    public static final String INST_RUNNING = "running";
    public static final String INST_OK = "ok";

    private static final long offsetInMilliseconds = 1000 * 60 * 10;

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
                .withSecurityGroups(SEC_GROUP)
                .withMonitoring(true);
        }
    }

    // Creates a new instance and returns the Instance ID
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

    public static Instance getInstance(String instanceId) {
        DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
        DescribeInstancesResult describeInstanceResult = ec2.describeInstances(describeInstanceRequest);
        Instance instance = new Instance();
        instance.setStatus(describeInstanceResult.getReservations().get(0).getInstances().get(0).getState().getCode());
        instance.setId(describeInstanceResult.getReservations().get(0).getInstances().get(0).getInstanceId());
        instance.setDns(describeInstanceResult.getReservations().get(0).getInstances().get(0).getPublicDnsName());
        return instance;
    }

    public static InstanceStatus getInstanceStatus(String instanceId) {
        DescribeInstanceStatusRequest disreq = new DescribeInstanceStatusRequest().withInstanceIds(instanceId);
        DescribeInstanceStatusResult disres = ec2.describeInstanceStatus(disreq);
        if (disres.getInstanceStatuses() != null && disres.getInstanceStatuses().size() > 0) {
            return disres.getInstanceStatuses().get(0);
        } else {
            return null;
        }
    }
}
