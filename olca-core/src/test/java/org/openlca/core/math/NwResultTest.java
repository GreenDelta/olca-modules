package org.openlca.core.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.NwSetTable;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.results.FullResult;

public class NwResultTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testSimpleResult() {
		// quantities and flows
		var units = db.insert(UnitGroup.of("Units of mass", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var e = db.insert(Flow.elementary("e", mass));
		var p = db.insert(Flow.product("p", mass));

		// process and product system
		var process = Process.of("process", p);
		process.output(e, 2);
		process = db.insert(process);
		var system = db.insert(ProductSystem.of(process));

		// LCIA method
		var impact = ImpactCategory.of("impact");
		impact.factor(e, 2);
		impact = db.insert(impact);
		var nwSet = NwSet.of("nw").add(NwFactor.of(impact, 0.5, 3));
		var method = ImpactMethod.of("method")
				.add(impact)
				.add(nwSet);
		method = db.insert(method);

		// calculate results
		var setup = CalculationSetup.fullAnalysis(system)
			.withImpactMethod(method)
			.withNwSet(nwSet);
		var result = FullResult.of(db, setup);
		var impacts = result.getTotalImpactResults();
		assertEquals(4, impacts.get(0).value(), 1e-10);

		// check nw results
		var factors = NwSetTable.of(db, nwSet);
		var normalized = factors.normalize(impacts);
		assertTrue(factors.hasNormalization());
		assertEquals(8, normalized.get(0).value(), 1e-10);
		assertTrue(factors.hasWeighting());
		var weighted = factors.weight(impacts);
		assertEquals(12, weighted.get(0).value(), 1e-10);
		var both = factors.apply(impacts);
		assertEquals(24, both.get(0).value(), 1e-10);
	}
}
