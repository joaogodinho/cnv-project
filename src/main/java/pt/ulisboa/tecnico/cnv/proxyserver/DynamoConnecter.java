package pt.ulisboa.tecnico.cnv.proxyserver;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

public final class DynamoConnecter {
	
	private static final String TABLE_NAME = "thread-metrics";
	private static final String PRIMARY_KEY = "instance-id";
	private static final String InstanceID = "wiiii";
    private static AmazonDynamoDBClient dynamoDB = null;

    private static AmazonDynamoDBClient getClient() {
    	if(dynamoDB == null) 
    		init();
    	return dynamoDB;
    }
	
	private static void init(){
		System.out.println("SUPPP");
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("NO CREDENTIALS FOUND!!!");
        }
        dynamoDB = new AmazonDynamoDBClient(credentials);
        dynamoDB.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
        CreateTableRequest table = new CreateTableRequest()
        		.withTableName(TABLE_NAME)
        		.withKeySchema(new KeySchemaElement(PRIMARY_KEY, KeyType.HASH))
        		.withAttributeDefinitions(new AttributeDefinition(PRIMARY_KEY, ScalarAttributeType.S))
        		.withProvisionedThroughput(new ProvisionedThroughput(1L,1L));
        TableUtils.createTableIfNotExists(dynamoDB, table);
        try{
        	TableUtils.waitUntilActive(dynamoDB, TABLE_NAME);
        }catch(Exception e){
        	e.printStackTrace();
        	System.out.println("Table took more than 10 minutes to be active!");
        }
        DynamoDB dynamo = new DynamoDB(getClient());
        Table instancesTable = dynamo.getTable(TABLE_NAME);
        Item item = new Item()
        		.withPrimaryKey(PRIMARY_KEY,InstanceID)
        		.withNumber("Threads", 0);
        		
        instancesTable.putItem(item);
    }
	
	public static boolean incrementThreadNumber(String instanceID){
		DynamoDB dynamo = new DynamoDB(getClient());
		Table instancesTable = dynamo.getTable(TABLE_NAME);
		Map<String,String> expressionAttributeNames = new HashMap<String,String>();
		expressionAttributeNames.put("#Threads", "Threads");
		Map<String,Object> expressionAttributeValues = new HashMap<String,Object>();
		expressionAttributeValues.put(":incr", 1);
		
		instancesTable.updateItem(
			    PRIMARY_KEY, InstanceID, 
			    "set #Threads = #Threads + :incr", 
			    expressionAttributeNames, 
			    expressionAttributeValues);
		return true;
	}
	
	public static boolean decrementThreadNumber(String instanceID){
		DynamoDB dynamo = new DynamoDB(getClient());
		Table instancesTable = dynamo.getTable(TABLE_NAME);
		Map<String,String> expressionAttributeNames = new HashMap<String,String>();
		expressionAttributeNames.put("#Threads", "Threads");
		Map<String,Object> expressionAttributeValues = new HashMap<String,Object>();
		expressionAttributeValues.put(":incr", 1);
		
		instancesTable.updateItem(
			    PRIMARY_KEY, InstanceID, 
			    "set #Threads = #Threads - :incr", 
			    expressionAttributeNames, 
			    expressionAttributeValues);
		return true;
	}
}
