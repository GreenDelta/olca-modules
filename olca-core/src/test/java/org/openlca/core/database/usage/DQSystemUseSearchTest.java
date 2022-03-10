package org.openlca.core.database.usage;

import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.Descriptor;

public class DQSystemUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private final UsageSearch search = UsageSearch.of(ModelType.DQ_SYSTEM, db);

	@Test
	public void testFindNoUsage() {
		var system = db.insert(DQSystem.of("dqs"));
		var models = search.find(system.id);
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
		db.delete(system);
	}

	@Test
	public void testFindInProcessDirect() {
		check((process, system) -> process.dqSystem = system);
	}

	@Test
	public void testFindInProcessExchange() {
		check((process, system) -> process.exchangeDqSystem = system);
	}

	@Test
	public void testFindInProcessSocial() {
		check((process, system) -> process.socialDqSystem = system);
	}

	private void check(BiConsumer<Process, DQSystem> fn) {
		var system = db.insert(DQSystem.of("dqs"));
		var process = new Process();
		fn.accept(process, system);
		db.insert(process);
		var results = search.find(system.id);
		db.delete(process, system);
		Descriptor expected = Descriptor.of(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.iterator().next());
	}

}
