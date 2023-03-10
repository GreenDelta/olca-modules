package org.openlca.core.database;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;

public class JpaIdentityTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testExchangeComposition() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var e = Flow.elementary("e", mass);
		var process = Process.of("P", p);
		db.insert(units, mass, p, e, process);
		var qRef = process.quantitativeReference;
		assertNotNull(qRef);

		var elemOut = process.output(e, 1);
		process = db.update(process);
		var alias = process.exchanges.stream()
				.filter(ex -> ex.flow.equals(e)).findAny().orElseThrow();

		// after an update, things are not the same (means are not identical)
		// anymore; even exchanges that were newly added to a process
		assertNotSame(qRef, process.quantitativeReference);
		assertNotSame(elemOut, alias);

		db.delete(process, p, e, mass, units);
	}
}
