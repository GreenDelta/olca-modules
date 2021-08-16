package org.openlca.validation;

import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

import static org.junit.Assert.*;

public class UnitCheckTest {

	private final IDatabase db = Tests.getDb();

	@Before
	public void before() {
		db.clear();
	}

	@Test
	public void testFindDuplicateUnitNames() {
		var u = UnitGroup.of("Group u", "u");
		u.units.add(Unit.of("d", 42));
		var v = UnitGroup.of("Group v", "v");
		v.units.add(Unit.of("d", 24));
		db.insert(u, v);

		var validation = Validation.on(db);
		validation.run();
		var item = validation.items()
			.stream()
			.filter(i -> i.hasModel()
				&& (i.model().id == u.id || i.model().id == v.id))
			.findAny()
			.orElse(null);

		assertNotNull(item);
		assertTrue(item.isWarning());
	}
}
