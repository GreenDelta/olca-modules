package com.greendelta.cloud.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;

import com.greendelta.cloud.model.data.DatasetIdentifier;
import com.greendelta.cloud.util.Directories;

public class DatasetIndexerTest {

	private final static File DIRECTORY = new File("C:/Users/Besitzer/tests/indexer");

	private DatasetIndexer indexer;

	@Before
	public void before() throws IOException {
		indexer = new DatasetIndexer(DIRECTORY);
	}

	@Test
	public void testIndexing() throws IOException {
		DatasetIdentifier expected = new DatasetIdentifier();
		expected.setRefId(UUID.randomUUID().toString());
		expected.setType(ModelType.ACTOR);
		expected.setLastChange(Calendar.getInstance().getTimeInMillis());
		expected.setVersion(new Version(1, 1, 1).toString());
		indexer.index(expected);
		DatasetIdentifier actual = indexer.get(ModelType.ACTOR, expected.getRefId());
		assertEquals(expected, actual);
		List<DatasetIdentifier> all = indexer.getAll();
		Assert.assertEquals(1, all.size());
		assertEquals(expected, all.get(0));
		indexer.delete(expected.getRefId());
		all = indexer.getAll();
		Assert.assertEquals(0, all.size());
		actual = indexer.get(ModelType.ACTOR, expected.getRefId());
		Assert.assertNull(actual);
	}

	@Test
	public void testMassIndexing() throws IOException {
		int amount = 100000;
		List<DatasetIdentifier> identifiers = new ArrayList<>();
		for (int i = 0; i < amount; i++)
			identifiers.add(createIdentifier());
		long time = Calendar.getInstance().getTimeInMillis();
		indexer.index(identifiers);
		printSeconds("Indexing", time);
		time = Calendar.getInstance().getTimeInMillis();
		List<DatasetIdentifier> all = indexer.getAll();
		printSeconds("Retrieving all " + amount + " documents", time);
		Assert.assertEquals(amount, all.size());
		DatasetIdentifier expected = identifiers.get(amount / 2);
		time = Calendar.getInstance().getTimeInMillis();
		DatasetIdentifier actual = indexer.get(ModelType.ACTOR, expected.getRefId());
		printSeconds("Retrieving document", time);
		assertEquals(expected, actual);
	}

	private void printSeconds(String task, long since) {
		long milli = Calendar.getInstance().getTimeInMillis() - since;
		System.out.println(task + " took " + milli + " ms");
	}

	private void assertEquals(DatasetIdentifier expected, DatasetIdentifier actual) {
		Assert.assertEquals(expected.getRefId(), actual.getRefId());
		Assert.assertEquals(expected.getType(), actual.getType());
		Assert.assertEquals(expected.getLastChange(), actual.getLastChange());
		Assert.assertEquals(expected.getVersion(), actual.getVersion());
	}

	private DatasetIdentifier createIdentifier() {
		DatasetIdentifier expected = new DatasetIdentifier();
		expected.setRefId(UUID.randomUUID().toString());
		expected.setType(ModelType.ACTOR);
		expected.setLastChange(Calendar.getInstance().getTimeInMillis());
		expected.setVersion(new Version(1, 1, 1).toString());
		return expected;
	}

	@After
	public void after() {
		Directories.delete(DIRECTORY);
	}

}
