package org.openlca.validation;

import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;

import static org.junit.Assert.assertTrue;

public class FlowDirectionCheckTest {

	private final IDatabase db = Tests.getDb();

	@Before
	public void before() {
		db.clear();
	}

	@Test
	public void testDirectionChecks() {

		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var product = Flow.product("pp", mass);
		var elem = Flow.elementary("ee", mass);
		var process = Process.of("p", product);

		// set the quantitative reference as product input
		// => this should generate a warning for the process
		process.quantitativeReference.isInput = true;

		// add the elementary flow as input and output
		// => this should generate an error for the flow
		process.input(elem, 1);
		process.output(elem, 2);

		db.insert(units, mass, product, elem, process);

		// run the validation and try to find the error and warning
		var validation = Validation.on(db);
		validation.run();
		boolean errorFound = false;
		boolean warningFound = false;
		for (var item : validation.items()) {
			if (item.model == null)
				continue;
			if (item.isError() && item.model.id == elem.id) {
				errorFound = true;
			} else if (item.isWarning() && item.model.id == process.id) {
				warningFound = true;
			}
		}

		assertTrue("could not find the flow error", errorFound);
		assertTrue("could not find the process warning", warningFound);

		db.delete(process, elem, product, mass, units);
	}
}
