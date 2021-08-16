package org.openlca.validation;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;

import static org.junit.Assert.*;

public class ValidationTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testGetInfoMessages() {
		var v = Validation.on(db);
		v.run();
		var okCount = v.items()
			.stream()
			.filter(Item::isOk)
			.count();
		assertTrue(okCount > 10);
	}

	@Test
	public void testSkipInfoMessages() {
		var v = Validation.on(db)
			.skipInfos(true);
		v.run();
		var okCount = v.items()
			.stream()
			.filter(Item::isOk)
			.count();
		assertEquals(0, okCount);
	}

	@Test
	public void testCancel() throws Exception {
		var v = Validation.on(db);
		var thread = new Thread(v);
		thread.start();
		v.cancel();
		assertTrue(v.wasCanceled());
		thread.join();
	}

	@Test(timeout = 30_000)
	public void testHasFinished() {
		var v = Validation.on(db);
		new Thread(v).start();
		while (true) {
			if (v.hasFinished())
				break;
		}
	}

	@Test
	public void testWorkerCount() {
		var v = Validation.on(db);
		assertEquals(0, v.finishedWorkerCount());
		v.run();
		assertTrue(v.finishedWorkerCount() > 10);
		assertEquals(v.workerCount(), v.finishedWorkerCount());
	}
}
