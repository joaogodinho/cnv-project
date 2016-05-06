package pt.ulisboa.tecnico.cnv.test;

import org.junit.Test;
import org.junit.Before;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cnv.proxyserver.AWS;

public class AWSInstancesTest {

    @Before
    public void initAWS() throws Exception { AWS.init(); }

    @Test
    public void createDeleteInstance() throws Exception {
        String instanceID;
        System.out.println("Creating new instance...");
        instanceID = AWS.createInstance();
        System.out.println("Created instance with ID = " + instanceID);
        System.out.println("Sleeping for 1min before terminating instance...");
        System.out.println(AWS.getInstance(instanceID));
        Thread.sleep(60000);
        System.out.println(AWS.getInstance(instanceID));
        System.out.println("Terminating instance...");
        AWS.terminateInstance(instanceID);
    }

    @Test
    public void createDeleteMultipleInstances() throws Exception {
        ArrayList<String> instanceIDs = new ArrayList<String>();
        System.out.println("Creating 5 instances...");
        for (int i = 0; i < 5; i++) {
            String id = AWS.createInstance();
            instanceIDs.add(id);
            System.out.println("Created instance with ID = " + id);
        }
        System.out.println("Sleeping for 1min before terminating instance...");
        Thread.sleep(60000);
        System.out.println("Shutting down instances...");
        for (String id: instanceIDs) {
            AWS.terminateInstance(id);
            System.out.println("Terminating instance with ID = " + id);
        }
    }
}
