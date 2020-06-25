package org.openlca.core.database.usage;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.DQSystemDescriptor;

public class DQSystemUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<DQSystemDescriptor> search;

	@Before
	public void setup() {
		search = IUseSearch.FACTORY.createFor(ModelType.DQ_SYSTEM, database);
	}

	@Test
	public void testFindNoUsage() {
		DQSystem system = createDqSystem();
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(system));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
		new DQSystemDao(database).delete(system);
	}

	private DQSystem createDqSystem() {
		DQSystem system = new DQSystem();
		system.name = "system";
		system = new DQSystemDao(database).insert(system);
		return system;
	}

	@Test
	public void testFindInProcessDirect() {
		DQSystem system = createDqSystem();
		Process process = new Process();
		process.dqSystem = system;
		new ProcessDao(database).insert(process);
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(system));
		new ProcessDao(database).delete(process);
		new DQSystemDao(database).delete(system);
		Descriptor expected = Descriptor.of(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	@Test
	public void testFindInProcessExchange() {
		DQSystem system = createDqSystem();
		Process process = new Process();
		process.exchangeDqSystem = system;
		new ProcessDao(database).insert(process);
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(system));
		new ProcessDao(database).delete(process);
		new DQSystemDao(database).delete(system);
		Descriptor expected = Descriptor.of(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	@Test
	public void testFindInProcessSocial() {
		DQSystem system = createDqSystem();
		Process process = new Process();
		process.socialDqSystem = system;
		new ProcessDao(database).insert(process);
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(system));
		new ProcessDao(database).delete(process);
		new DQSystemDao(database).delete(system);
		Descriptor expected = Descriptor.of(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

}
