package org.openlca.core.matrix.uncertainties;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UnitGroup;
import org.openlca.expressions.FormulaInterpreter;

public class MatrixSimulationTest {

	@Test
	public void testSimpleExchange() {

		// create a simple system
		var db = Tests.getDb();
		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var steel = db.insert(Flow.product("Steel", mass));
		var co2 = db.insert(Flow.elementary("CO2", mass));
		var p = Process.of("p", steel);
		var e = p.output(co2, 2);
		e.uncertainty = Uncertainty.logNormal(10, 1.4);
		db.insert(p);
		var system = db.insert(ProductSystem.of(p));

		var data = MatrixData.of(db, TechIndex.of(db, system))
			.withUncertainties(true)
			.build();
		assertEquals(2.0, data.enviMatrix.get(0, 0), 1e-16);

		var interpreter = new FormulaInterpreter();
		var min = Double.MAX_VALUE;
		var max = -Double.MAX_VALUE;
		for (int i = 0; i < 100; i++) {
			data.simulate(interpreter);
			double value = data.enviMatrix.get(0, 0);
			min = Math.min(min, value);
			max = Math.max(max, value);
		}

		assertTrue(min < max);

		// delete the models
		List.of(system, p, steel, co2, mass, units)
			.forEach(db::delete);
	}

}
