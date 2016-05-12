import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import pt.ulisboa.tecnico.cnv.proxyserver.DynamoConnecter;

public class StatisticsTableTest{

	@Test
	public void insertNewEntry(){
		DynamoConnecter.createStatisticsTable();
		int id = DynamoConnecter.addStatisticEntry(20, 100);
		List<Long> list = DynamoConnecter.getInstructionCountForNumberBits(20);
		assertTrue(list.get(0) == 100);
		DynamoConnecter.removeStatisticEntry(id);
		list = DynamoConnecter.getInstructionCountForNumberBits(20);
		assertTrue(list.isEmpty());
	}
	
	@Test
	public void insertMultipleGetMultiple(){
		DynamoConnecter.createStatisticsTable();
		int id1 = DynamoConnecter.addStatisticEntry(20, 100);
		int id2 = DynamoConnecter.addStatisticEntry(20, 200);
		int id3 = DynamoConnecter.addStatisticEntry(30, 100);
		int id4 = DynamoConnecter.addStatisticEntry(30, 1000);
		List<Long> trinta = DynamoConnecter.getInstructionCountForNumberBits(30);
		List<Long> vinte = DynamoConnecter.getInstructionCountForNumberBits(20);
		assertTrue(trinta.size() == 2);
		assertTrue(vinte.size() == 2);
		DynamoConnecter.removeStatisticEntry(id1);
		DynamoConnecter.removeStatisticEntry(id2);
		DynamoConnecter.removeStatisticEntry(id3);
		DynamoConnecter.removeStatisticEntry(id4);
	}
	
	@Test
	public void getNonExistingStatistic(){
		DynamoConnecter.createStatisticsTable();
		List<Long> empty = DynamoConnecter.getInstructionCountForNumberBits(40);
		assertTrue(empty.isEmpty());
	}
	
}
