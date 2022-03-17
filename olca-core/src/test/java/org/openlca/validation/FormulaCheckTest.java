package org.openlca.validation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;

import static org.junit.Assert.assertTrue;

/**
 * Tests that we can find all possible formula errors in a database. A
 * formula error should result in a validation error that contains the
 * erroneous formula in the validation message.
 */
public class FormulaCheckTest {

	private final IDatabase db = Tests.getDb();
	private FlowProperty mass;

	@Before
	public void before() {
		db.clear();
		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		mass = db.insert(FlowProperty.of("Mass", units));
	}

	@After
	public void after() {
		db.delete(mass);
		db.delete(mass.unitGroup);
	}

	@Test
	public void testInGlobalParameter() {
		var formula = "1 + "; // syntax error
		var param = db.insert(Parameter.global("param", formula));
		check(param, formula);
		db.delete(param);
	}

	@Test
	public void testInLocalParameter() {
		var formula = "2 * unknown(42)"; // unknown function
		var product = db.insert(Flow.product("pp", mass));
		var process = Process.of("p", product);
		process.parameters.add(Parameter.process("param", formula));
		db.insert(process);
		check(process, formula);
		db.delete(process, product);
	}

	@Test
	public void testInExchangeAmount() {
		var formula = "x + y"; // unbound parameters
		var product = db.insert(Flow.product("pp", mass));
		var process = Process.of("p", product);
		process.quantitativeReference.formula = formula;
		db.insert(process);
		check(process, formula);
		db.delete(process, product);
	}

	@Test
	public void testInExchangeCosts() {
		var formula = "( 1 * 2 "; // syntax error
		var product = db.insert(Flow.product("pp", mass));
		var process = Process.of("p", product);
		process.quantitativeReference.costFormula = formula;
		db.insert(process);
		check(process, formula);
		db.delete(process, product);
	}

	@Test
	public void testInAllocationFactor() {
		var formula = " if( true, 2) "; // not enough arguments
		var product = db.insert(Flow.product("pp", mass));
		var waste = db.insert(Flow.waste("ww", mass));
		var process = Process.of("p", product);
		process.output(waste, 1);
		var factor = AllocationFactor.physical(waste, 0);
		factor.formula = formula;
		process.allocationFactors.add(factor);
		db.insert(process);
		check(process, formula);
		db.delete(process, product, waste);
	}

	@Test
	public void testInImpactFactor() {
		var formula = " sin(5, 3) "; // to many parameters
		var flow = db.insert(Flow.elementary("e", mass));
		var impact = ImpactCategory.of("impact");
		impact.factor(flow, 42).formula = formula;
		db.insert(impact);
		check(impact, formula);
		db.delete(impact, flow);
	}

	private void check(RootEntity model, String formula) {
		var validation = Validation.on(db);
		validation.run();
		boolean found = false;
		for (var item : validation.items()) {
			if (!item.isError() || item.message == null)
				continue;
			if (!item.message.contains(formula))
				continue;
			if (item.model == null || item.model.id != model.id)
				continue;
			found = true;
		}
		assertTrue("Could not find formula error for: '"
							 + formula + "' in model " + model, found);
	}
}
