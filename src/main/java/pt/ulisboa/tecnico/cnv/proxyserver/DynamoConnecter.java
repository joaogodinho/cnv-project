package pt.ulisboa.tecnico.cnv.proxyserver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import pt.ulisboa.tecnico.cnv.httpserver.HTTPServer;

public final class DynamoConnecter {
	
	private static final String TABLE_NAME = "thread-metrics";
	private static final String PRIMARY_KEY = "instance-id";
	private static final String CRUNCHING_TABLE = "instant-metrics";
	private static final String PRIMARY_KEY_CRUNCHING = "unique-id";
    private static AmazonDynamoDBClient dynamoDB = null;
    private static int id = 0;
    
    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    
    private static synchronized int getUniqueID(){
    	return id++;
    }
    
    
    private static AmazonDynamoDBClient getClient() {
    	if(dynamoDB == null) 
    		init();
    	return dynamoDB;
    }
	
	private static void init(){
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
        		.withPrimaryKey(PRIMARY_KEY,HTTPServer.instanceID)
        		.withNumber("Threads", 0);
        if(HTTPServer.instanceID != null)
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
			    PRIMARY_KEY, instanceID, 
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
			    PRIMARY_KEY, instanceID, 
			    "set #Threads = #Threads - :incr", 
			    expressionAttributeNames, 
			    expressionAttributeValues);
		return true;
	}

	public static void createCrunchingTable() {
		AmazonDynamoDBClient client = getClient();
		CreateTableRequest table = new CreateTableRequest()
        		.withTableName(CRUNCHING_TABLE)
        		.withKeySchema(new KeySchemaElement(PRIMARY_KEY_CRUNCHING, KeyType.HASH))
        		.withAttributeDefinitions(new AttributeDefinition(PRIMARY_KEY_CRUNCHING, ScalarAttributeType.S))
        		.withProvisionedThroughput(new ProvisionedThroughput(1L,1L));
		TableUtils.createTableIfNotExists(client, table);
		try{
        	TableUtils.waitUntilActive(client, CRUNCHING_TABLE);
        }catch(Exception e){
        	e.printStackTrace();
        	System.out.println("Table took more than 10 minutes to be active!");
        }
	}
	
	public static NumberCrunchingEntry createEntryGetID(String instance, int number_bits){
		int entry_id = getUniqueID();
		Map<String, AttributeValue> newItem = createItem(entry_id,instance,number_bits);
		String timestamp = newItem.get("starting_time").getS();
		PutItemRequest putItemRequest = new PutItemRequest(CRUNCHING_TABLE, newItem);
        getClient().putItem(putItemRequest);
		return new NumberCrunchingEntry(entry_id,timestamp,number_bits);
	}
	
	public static void deleteEntry(int entry_id){
		AmazonDynamoDBClient client = getClient();
		DeleteItemRequest delete = new DeleteItemRequest()
				.withTableName(CRUNCHING_TABLE)
				.addKeyEntry(PRIMARY_KEY_CRUNCHING, new AttributeValue(Integer.toString(entry_id)));
		client.deleteItem(delete);
	}
	
	private static Map<String,AttributeValue> createItem(int entry_id, String instance, int number_bits){
		Map<String,AttributeValue> item = new HashMap<String,AttributeValue>();
		item.put(PRIMARY_KEY_CRUNCHING, new AttributeValue(Integer.toString(entry_id)));
		item.put("instanceID", new AttributeValue(instance));
		item.put("number_bits",new AttributeValue(Integer.toString(number_bits)));
		item.put("starting_time", new AttributeValue(dateFormat.format(new Date())));
		return item;
	}
	
	public static void getRunningNumbersOnServer(String instance){
		HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
        Condition condition = new Condition()
            .withComparisonOperator(ComparisonOperator.EQ.toString())
            .withAttributeValueList(new AttributeValue(instance));
        scanFilter.put("instanceID", condition);
        ScanRequest scanrequest = new ScanRequest(CRUNCHING_TABLE).withScanFilter(scanFilter);
        ScanResult scanresult = getClient().scan(scanrequest);
        System.out.println("got " + scanresult + " running on " + instance);
	}
}
