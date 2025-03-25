package org.openlca.validation;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;

public class FlowDirectionCheckTest {

	private final IDatabase db = Tests.getDb();

	private Flow elem;
	private Process process;

	@Before
	public void before() {
		db.clear();
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var product = Flow.product("pp", mass);
		elem = Flow.elementary("ee", mass);
		process = Process.of("p", product);
		db.insert(units, mass, product, elem, process);
	}

	@After
	public void after() {
		db.delete(
				elem.referenceFlowProperty.unitGroup,
				elem.referenceFlowProperty,
				elem,
				process.quantitativeReference.flow,
				process
		);
	}

	@Test
	public void testAllGood() {
		process.output(elem, 42);
		process = db.update(process);
		var validation = Validation.on(db);
		validation.run();
		var errs = validation.items()
				.stream()
				.filter(e -> e.isError() || e.isWarning())
				.toList();
		assertTrue(errs.isEmpty());
	}

	@Test
	public void testRefProductIsInput() {
		// set the quantitative reference as product input
		// => this should generate a warning for the process
		process.quantitativeReference.isInput = true;
		process = db.update(process);
		var validation = Validation.on(db);
		validation.run();
		var err = validation.items()
				.stream()
				.filter(Item::isWarning)
				.findFirst()
				.orElseThrow();
		assertEquals(err.model.id, process.id);
	}

	@Test
	public void testDirectionChecks() {
		// add the elementary flow as input and output
		// => this should generate a warning for the flow
		process.input(elem, 1);
		process.output(elem, 2);
		process = db.update(process);

		var validation = Validation.on(db);
		validation.run();
		var err = validation.items()
				.stream()
				.filter(Item::isWarning)
				.findFirst()
				.orElseThrow();
		assertEquals(err.model.id, elem.id);
	}
}
