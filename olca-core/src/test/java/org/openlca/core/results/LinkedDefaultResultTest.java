package org.openlca.core.results;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;

public class LinkedDefaultResultTest {

	private final IDatabase db = Tests.getDb();
	private Process Q;
	private ImpactMethod method;
	private List<RootEntity> entities;

	@Before
	public void setup() {

		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var impact = ImpactCategory.of("GWP 100", "CO2eq");
		method = ImpactMethod.of("Method");
		method.impactCategories.add(impact);

		var p = Flow.product("p", mass);
		var R = Result.of("R", p);
		R.impactMethod = method;
		R.impactResults.add(ImpactResult.of(impact, 21));
		db.insert(units, mass, impact, method, p, R);

		var q = Flow.product("q", mass);
		Q = Process.of("Q", q);
		var inp = Q.input(p, 2);
		inp.defaultProviderType = ProviderType.RESULT;
		inp.defaultProviderId = R.id;
		db.insert(q, Q);

		entities = List.of(Q, q, R, p, method, impact, mass, units);
	}

	@After
	public void cleanup() {
		for (var e : entities) {
			db.delete(e);
		}
	}

	@Test
	public void testTechIndex() {
		var setup = CalculationSetup.of(Q)
				.withImpactMethod(method);
		var techIdx = TechIndex.of(db, setup);
		assertEquals(2, techIdx.size());
	}

	@Test
	public void directCalculation() {
		var setup = CalculationSetup.of(Q)
				.withImpactMethod(method);
		var r = new SystemCalculator(db).calculate(setup);
		var I = r.impactIndex().at(0);
		assertEquals(42.0, r.getTotalImpactValueOf(I), 1e-10);
	}

}
