package org.openlca.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.UnitGroup;

public class NativeSqlTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testRawConnection() throws Exception {
		var query = "select count(*) from tbl_units";
		try (var con = db.createConnection();
				 var stmt = con.createStatement();
				 var results = stmt.executeQuery(query)) {
			assertTrue(results.next());
			long count = results.getLong(1);
			assertTrue(count >= 0);
		}
	}

	@Test
	public void testSecureQuery() {
		var query = "select * from tbl_unit_groups where name = ?";
		var evilParam = "''; delete from tbl_flows";
		var count = new AtomicInteger(0);
		NativeSql.on(db).query(query, List.of(evilParam), r -> {
			count.incrementAndGet();
			return true;
		});
		assertEquals(0, count.get());

		var units = db.insert(UnitGroup.of("friendly units"));
		NativeSql.on(db).query(query, List.of("friendly units"), r -> {
			count.incrementAndGet();
			return true;
		});
		assertEquals(1, count.get());
		db.delete(units);
	}
}
