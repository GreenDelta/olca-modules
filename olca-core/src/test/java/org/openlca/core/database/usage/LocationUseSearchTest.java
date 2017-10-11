package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.LocationDescriptor;

public class LocationUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<LocationDescriptor> search;
	private Location location;

	@Before
	public void setup() {
		location = new Location();
		location.setName("location");
		location = new LocationDao(database).insert(location);
		search = IUseSearch.FACTORY.createFor(ModelType.LOCATION, database);
	}

	@After
	public void tearDown() {
		new LocationDao(database).delete(location);
	}

	@Test
	public void testFindNoUsage() {
		List<CategorizedDescriptor> models = search.findUses(Descriptors
				.toDescriptor(location));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInFlows() {
		Flow flow = createFlow();
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(location));
		new FlowDao(database).delete(flow);
		BaseDescriptor expected = Descriptors.toDescriptor(flow);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Flow createFlow() {
		Flow flow = new Flow();
		flow.setName("flow");
		flow.setLocation(location);
		return new FlowDao(database).insert(flow);
	}

	@Test
	public void testFindInProcesses() {
		Process process = createProcess();
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(location));
		new ProcessDao(database).delete(process);
		BaseDescriptor expected = Descriptors.toDescriptor(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Process createProcess() {
		Process process = new Process();
		process.setName("process");
		process.setLocation(location);
		return new ProcessDao(database).insert(process);
	}
}
