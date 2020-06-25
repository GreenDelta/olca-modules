package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ProcessUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<ProcessDescriptor> search;
	private Process process;

	@Before
	public void setup() {
		process = new Process();
		process.name = "process";
		process = new ProcessDao(database).insert(process);
		search = IUseSearch.FACTORY.createFor(ModelType.PROCESS, database);
	}

	@After
	public void tearDown() {
		new ProcessDao(database).delete(process);
	}

	@Test
	public void testFindNoUsage() {
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(process));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInSystems() {
		ProductSystem system = createSystem();
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(process));
		new ProductSystemDao(database).delete(system);
		Descriptor expected = Descriptor.of(system);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private ProductSystem createSystem() {
		ProductSystem system = new ProductSystem();
		system.name = "system";
		system.processes.add(process.id);
		return new ProductSystemDao(database).insert(system);
	}

}
