package org.openlca.core.database;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Result;

public class ResultIOTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testSimpleIO() {
		var r = Result.of("result");
		db.insert(r);
		db.clearCache();
		var clone = db.get(Result.class, r.id);
		assertEquals(r, clone);
		var dao = new ResultDao(db);
		var all = dao.getAll();
		assertTrue(all.contains(r));
		db.delete(r);
		all = dao.getAll();
		assertFalse(all.contains(r));
	}

}
