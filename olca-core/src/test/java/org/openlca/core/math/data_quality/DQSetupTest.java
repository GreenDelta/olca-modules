package org.openlca.core.math.data_quality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.stream.IntStream;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Process;

public class DQSetupTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testConsistentSetup() {

		// no processes in the database => no consistent setup
		assertTrue(DQSetup.consistentOf(db).isEmpty());

		// no processes with DQ systems => no consistent setup
		var processes = IntStream.range(0, 5)
				.mapToObj(i -> db.insert(Process.of("p " + i, null)))
				.toList();
		assertTrue(DQSetup.consistentOf(db).isEmpty());

		// processes with common exchange DQS => ok
		var exchangeDqs = db.insert(new DQSystem());
		processes = processes.stream().map(p -> {
			p.exchangeDqSystem = exchangeDqs;
			return db.update(p);
		}).toList();
		var setup = DQSetup.consistentOf(db).orElseThrow();
		assertEquals(setup.exchangeSystem, exchangeDqs);
		assertNull(setup.processSystem);

		// processes with exchange and process DQSs => ok
		var processDqs = db.insert(new DQSystem());
		processes = processes.stream().map(p -> {
			p.dqSystem = processDqs;
			return db.update(p);
		}).toList();
		setup = DQSetup.consistentOf(db).orElseThrow();
		assertEquals(exchangeDqs, setup.exchangeSystem);
		assertEquals(processDqs, setup.processSystem);

		// null values are ok
		processes = processes.stream().map(p -> {
			if (p.name.equals("p 3")) {
				p.dqSystem = null;
				p.exchangeDqSystem = null;
				return db.update(p);
			}
			return p;
		}).toList();
		setup = DQSetup.consistentOf(db).orElseThrow();
		assertEquals(exchangeDqs, setup.exchangeSystem);
		assertEquals(processDqs, setup.processSystem);

		// mixed setups are not ok
		processes = processes.stream().map(p -> {
			if (p.name.equals("p 3")) {
				p.dqSystem = exchangeDqs;
				p.exchangeDqSystem = processDqs;
				return db.update(p);
			}
			return p;
		}).toList();
		assertTrue(DQSetup.consistentOf(db).isEmpty());

		// cleanup
		processes.forEach(db::delete);
		db.delete(exchangeDqs);
	}
}
