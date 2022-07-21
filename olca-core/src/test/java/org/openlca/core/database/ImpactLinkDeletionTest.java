package org.openlca.core.database;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;

public class ImpactLinkDeletionTest {

	private final IDatabase db = Tests.getDb();

	@Before
	public void setup() {
		// todo: this is currently necessary because other
		// tests do not delete their data (but they should)
		db.clear();
	}

	@Test
	public void testDeleteLinks() {

		var impact = ImpactCategory.of("GWP");
		var method1 = ImpactMethod.of("M1");
		method1.impactCategories.add(impact);
		var method2 = ImpactMethod.of("M2");
		method2.impactCategories.add(impact);
		db.insert(impact, method1, method2);

		check("tbl_impact_methods", 2);
		check("tbl_impact_categories", 1);
		check("tbl_impact_links", 2);
		db.delete(method1, method2, impact);
		check("tbl_impact_methods", 0);
		check("tbl_impact_categories", 0);
		check("tbl_impact_links", 0);
	}

	private void check(String table, int expectedRows) {
		var count = new AtomicInteger(0);
		NativeSql.on(db).query("select * from " + table, r -> {
			count.incrementAndGet();
			return true;
		});
		assertEquals(expectedRows, count.get());
	}

}
