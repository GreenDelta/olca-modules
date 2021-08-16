package org.openlca.validation;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.validation.Item.Type;

import static org.junit.Assert.fail;

public class ProcessCheckTest {

	private static final IDatabase db = Tests.getDb();

	@BeforeClass
	public static void setup() {
		db.clear();
	}

	@Test
	public void testAllocation() {
		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var co2 = db.insert(Flow.elementary("CO2", mass));
		var p1 = db.insert(Flow.product("p1", mass));
		var p2 = db.insert(Flow.product("p2", mass));

		var process = Process.of("p", p1);
		process.output(p2, 1.0);
		var co2Output = process.output(co2, 5.0);
		process.allocationFactors.add(
			AllocationFactor.causal(p1, co2Output, 0.7));
		db.insert(process);

		NativeSql.on(db).runUpdate(
			"update tbl_allocation_factors set f_exchange = 1");
		var validation = Validation.on(db);
		validation.run();
		var found = false;
		for (var item : validation.items()) {
			if (item.type == Type.ERROR) {
				if (item.model != null && item.model.id == process.id) {
					found = true;
				}
			}
		}
		if (!found) {
			fail("expected an error because of invalid exchange ID");
		}
	}

}
