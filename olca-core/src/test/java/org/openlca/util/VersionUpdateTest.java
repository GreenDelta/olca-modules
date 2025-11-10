package org.openlca.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;

import gnu.trove.set.hash.TLongHashSet;

public class VersionUpdateTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testVersionUpdate() {
		long t0 = System.currentTimeMillis();
		var units = UnitGroup.of("Mass units", "kg");
		units.version = 1L;
		units.lastChange = t0;
		units = db.insert(units);
		VersionUpdate.of(db, UnitGroup.class).run(units.id);

		var tSet = new TLongHashSet();
		tSet.add(units.id);
		VersionUpdate.of(db, "tbl_unit_groups").run(tSet);

		var jSet = new HashSet<Long>();
		jSet.add(units.id);
		VersionUpdate.of(db, UnitGroup.class).run(jSet);

		var sinSet = TLongSets.singleton(units.id);
		VersionUpdate.of(db, ModelType.UNIT_GROUP).run(sinSet);

		units = db.get(UnitGroup.class, units.id);
		assertEquals(5L, units.version);
		assertTrue(t0 < units.lastChange);
		db.delete(units);
	}


}
