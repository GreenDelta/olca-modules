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
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

public class LocationUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<LocationDescriptor> search;
	private Location location;

	@Before
	public void setup() {
		location = new Location();
		location.name = "location";
		location = new LocationDao(database).insert(location);
		search = IUseSearch.FACTORY.createFor(ModelType.LOCATION, database);
	}

	@After
	public void tearDown() {
		new LocationDao(database).delete(location);
	}

	@Test
	public void testFindNoUsage() {
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(location));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInFlows() {
		Flow flow = createFlow();
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(location));
		new FlowDao(database).delete(flow);
		Descriptor expected = Descriptor.of(flow);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Flow createFlow() {
		Flow flow = new Flow();
		flow.name = "flow";
		flow.location = location;
		return new FlowDao(database).insert(flow);
	}

	@Test
	public void testFindInProcesses() {
		Process process = createProcess();
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(location));
		new ProcessDao(database).delete(process);
		Descriptor expected = Descriptor.of(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Process createProcess() {
		Process process = new Process();
		process.name = "process";
		process.location = location;
		return new ProcessDao(database).insert(process);
	}
}
