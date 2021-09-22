package org.openlca.core.database;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.ResultModel;
import org.openlca.core.model.descriptors.Descriptor;

public class ResultIOTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testWithSubResults() {
		var top = ResultModel.of("top");
		var sub1 = ResultModel.of("sub1");
		var sub2 = ResultModel.of("sub2");
		top.subResults.add(sub1);
		top.subResults.add(sub2);
		db.insert(top);
		db.clearCache();

		var clone = db.get(ResultModel.class, top.id);
		assertEquals(top, clone);
		assertEquals(2, clone.subResults.size());
		for (var sub : clone.subResults) {
			assertTrue(sub.equals(sub1) || sub.equals(sub2));
		}

		var dao = new ResultDao(db);

		var all = dao.getAll();
		assertTrue(all.contains(top));
		assertTrue(all.contains(sub1));
		assertTrue(all.contains(sub2));

		var topResults = dao.getTopResults();
		assertTrue(topResults.contains(Descriptor.of(top)));
		assertFalse(topResults.contains(Descriptor.of(sub1)));
		assertFalse(topResults.contains(Descriptor.of(sub2)));

		db.delete(top);

		all = dao.getAll();
		assertFalse(all.contains(top));
		assertFalse(all.contains(sub1));
		assertFalse(all.contains(sub2));
	}

}
