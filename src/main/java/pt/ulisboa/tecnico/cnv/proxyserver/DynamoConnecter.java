package pt.ulisboa.tecnico.cnv.proxyserver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
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

public final class DynamoConnecter {
	
	private static final String THREADS_TABLE = "thread-metrics";
	private static final String CRUNCHING_TABLE = "instant-metrics";
	private static final String STATISTICS_TABLE = "statistics";
	private static final String PRIMARY_KEY_THREADS = "instance-id";
	private static final String PRIMARY_KEY_CRUNCHING = "unique-id";
	private static final String PRIMARY_KEY_STATISTICS = "unique-id";
    private static AmazonDynamoDBClient dynamoDB = null;
    private static int id = 0;
    private static int stat_id = 0;
    
    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    
    private static synchronized int getUniqueID(){
    	return id++;
    }
    
    private static synchronized int getStatisticsID(){
    	return stat_id++;
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
    }
	
	public static void createTables(){
		createThreadTable();
		createCrunchingTable();
		createStatisticsTable();
	}
	
	
	public static void createThreadTable(){
		AmazonDynamoDBClient client = getClient();
		CreateTableRequest table = new CreateTableRequest()
        		.withTableName(THREADS_TABLE)
        		.withKeySchema(new KeySchemaElement(PRIMARY_KEY_THREADS, KeyType.HASH))
        		.withAttributeDefinitions(new AttributeDefinition(PRIMARY_KEY_THREADS, ScalarAttributeType.S))
        		.withProvisionedThroughput(new ProvisionedThroughput(1L,1L));
        TableUtils.createTableIfNotExists(client, table);
        
        try{
        	TableUtils.waitUntilActive(client, THREADS_TABLE);
        }catch(Exception e){
        	e.printStackTrace();
        	System.out.println("Table Threads took more than 10 minutes to be active!");
        }
	}
	
	public static boolean incrementThreadNumber(String instanceID){
		DynamoDB dynamo = new DynamoDB(getClient());
		Table instancesTable = dynamo.getTable(THREADS_TABLE);
		Map<String,String> expressionAttributeNames = new HashMap<String,String>();
		expressionAttributeNames.put("#Threads", "Threads");
		Map<String,Object> expressionAttributeValues = new HashMap<String,Object>();
		expressionAttributeValues.put(":incr", 1);
		
		instancesTable.updateItem(
			    PRIMARY_KEY_THREADS, instanceID, 
			    "set #Threads = #Threads + :incr", 
			    expressionAttributeNames, 
			    expressionAttributeValues);
		return true;
	}
	
	public static boolean decrementThreadNumber(String instanceID){
		DynamoDB dynamo = new DynamoDB(getClient());
		Table instancesTable = dynamo.getTable(THREADS_TABLE);
		Map<String,String> expressionAttributeNames = new HashMap<String,String>();
		expressionAttributeNames.put("#Threads", "Threads");
		Map<String,Object> expressionAttributeValues = new HashMap<String,Object>();
		expressionAttributeValues.put(":incr", 1);
		
		instancesTable.updateItem(
			    PRIMARY_KEY_THREADS, instanceID, 
			    "set #Threads = #Threads - :incr", 
			    expressionAttributeNames, 
			    expressionAttributeValues);
		return true;
	}
	
	//Crunching table

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
        	System.out.println("Table Crunching took more than 10 minutes to be active!");
        }
	}
	
	//Add a new entry that is being run on one of the instances
	
	public static NumberCrunchingEntry createEntryGetID(String instance, int number_bits){
		int entry_id = getUniqueID();
		Map<String, AttributeValue> newItem = createNumberCrunchingEntry(entry_id,instance,number_bits);
		String timestamp = newItem.get("starting_time").getS();
		PutItemRequest putItemRequest = new PutItemRequest(CRUNCHING_TABLE, newItem);
        getClient().putItem(putItemRequest);
		return new NumberCrunchingEntry(entry_id,timestamp,number_bits);
	}
	
	
	private static Map<String,AttributeValue> createNumberCrunchingEntry(int entry_id, String instance, int number_bits){
		Map<String,AttributeValue> item = new HashMap<String,AttributeValue>();
		item.put(PRIMARY_KEY_CRUNCHING, new AttributeValue(Integer.toString(entry_id)));
		item.put("instanceID", new AttributeValue(instance));
		item.put("number_bits",new AttributeValue(Integer.toString(number_bits)));
		item.put("starting_time", new AttributeValue(dateFormat.format(new Date())));
		item.put("instruction_count", new AttributeValue().withN(Integer.toString(0)));
		return item;
	}
	
	
	//delete a finished entry
	
	public static void deleteEntry(int entry_id){
		AmazonDynamoDBClient client = getClient();
		DeleteItemRequest delete = new DeleteItemRequest()
				.withTableName(CRUNCHING_TABLE)
				.addKeyEntry(PRIMARY_KEY_CRUNCHING, new AttributeValue(Integer.toString(entry_id)));
		client.deleteItem(delete);
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


	//Increments Number of instructions, used on the instances 
	public static void incrementInstructions(String unique_id, String instruction_number) {
		DynamoDB dynamo = new DynamoDB(getClient());
		Table instancesTable = dynamo.getTable(CRUNCHING_TABLE);
		Map<String,String> expressionAttributeNames = new HashMap<String,String>();
		expressionAttributeNames.put("#instructions", "instruction_count");
		Map<String,Object> expressionAttributeValues = new HashMap<String,Object>();
		expressionAttributeValues.put(":incr", Integer.valueOf(instruction_number));
		
		instancesTable.updateItem(
			    PRIMARY_KEY_CRUNCHING, unique_id, 
			    "set #instructions = #instructions + :incr", 
			    expressionAttributeNames, 
			    expressionAttributeValues);
	}
	
	//get the number of instructions up to this time of a specific NumberCrunchingEntry
	public static long getNumberOfInstructions(String unique_id){
		HashMap<String,Condition> scanFilter = new HashMap<String,Condition>();
		Condition condition = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue(unique_id));
		scanFilter.put("unique_id", condition);
		ScanRequest scanrequest = new ScanRequest(CRUNCHING_TABLE).withScanFilter(scanFilter);
		ScanResult result = getClient().scan(scanrequest);
		Map<String,AttributeValue> items = result.getItems().get(0); //returns a list of objects, we only need the first
		String instructions = items.get("instruction_count").getS();
		return Long.parseLong(instructions);
	}
	
	
	//Statistics Table
	public static void createStatisticsTable(){
		AmazonDynamoDBClient client = getClient();
		CreateTableRequest table = new CreateTableRequest()
				.withTableName(STATISTICS_TABLE)
				.withKeySchema(new KeySchemaElement(PRIMARY_KEY_STATISTICS, KeyType.HASH))
				.withAttributeDefinitions(new AttributeDefinition(PRIMARY_KEY_STATISTICS,ScalarAttributeType.S))
				.withProvisionedThroughput(new ProvisionedThroughput(1L,1L));
		TableUtils.createTableIfNotExists(client, table);
		try{
			TableUtils.waitUntilActive(client, STATISTICS_TABLE);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Table Statistics took more than 10 minutes to be active!");
		}
	}
	
	public static void removeStatisticEntry(int unique_id){
		AmazonDynamoDBClient client = getClient();
		DeleteItemRequest delete = new DeleteItemRequest()
				.withTableName(STATISTICS_TABLE)
				.addKeyEntry(PRIMARY_KEY_STATISTICS, new AttributeValue(Integer.toString(unique_id)));
		client.deleteItem(delete);
	}
	
	
	//add a new completed entry with number_bits that was completed in number_instructions
	public static int addStatisticEntry(int number_bits, long number_instructions){
		AmazonDynamoDBClient client = getClient();
		int unique_id = getStatisticsID();
		Map<String,AttributeValue> newItem = new HashMap<String,AttributeValue>();
		newItem.put(PRIMARY_KEY_STATISTICS, new AttributeValue().withS(String.valueOf(unique_id)));
		newItem.put("number_bits",new AttributeValue(String.valueOf(number_bits)));
		newItem.put("number_instructions", new AttributeValue(Long.toString(number_instructions)));
		PutItemRequest putItemRequest = new PutItemRequest(STATISTICS_TABLE, newItem);
		client.putItem(putItemRequest);
		return unique_id;
	}
	
	
	//returns a list of Longs with all the instruction count completed so far for number_bits
	public static List<Long> getInstructionCountForNumberBits(int number_bits){
		AmazonDynamoDBClient client = getClient();
		HashMap<String,Condition> scanFilter = new HashMap<String,Condition>();
		Condition condition = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue(String.valueOf(number_bits)));
		scanFilter.put("number_bits", condition);
		ScanRequest scanrequest = new ScanRequest(STATISTICS_TABLE).withScanFilter(scanFilter);
		ScanResult result = client.scan(scanrequest);
		ArrayList<Long> list = new ArrayList<Long>();
		for(Map<String,AttributeValue> item : result.getItems()){
			
			list.add(Long.parseLong(item.get("number_instructions").getS()));
		}
		return list;
	}
	
}
