package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.Result;
import org.openlca.core.model.UnitGroup;

public class ResultUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private UnitGroup units;
	private FlowProperty mass;
	private Flow product;
	private Result result;

	@Before
	public void setup() {
		units = UnitGroup.of("Units of mass", "kg");
		mass = FlowProperty.of("Mass", units);
		product = Flow.product("p", mass);
		result = Result.of("result", product);
		db.insert(units, mass, product, result);
	}

	@After
	public void cleanup() {
		db.delete(result, product, mass, units);
	}

	@Test
	public void testNoUsage() {
		UsageTests.expectEmpty(result);
	}

	@Test
	public void testInProcessAndSystem() {
		var q = Flow.product("q", mass);
		var Q = Process.of("Q", q);
		var input = Q.input(product, 1.0);
		input.defaultProviderId = result.id;
		input.defaultProviderType = ProviderType.RESULT;
		db.insert(q, Q);

		var sys = ProductSystem.of("sys", Q);
		sys.link(TechFlow.of(result), Q);
		db.insert(sys);

		UsageTests.expectEach(result, Q, sys);
		db.delete(sys, Q, q);
		UsageTests.expectEmpty(result);
	}

	@Test
	public void testInEPD() {
		var epd = Epd.of("EPD", product);
		var mod = EpdModule.of("mod", result);
		epd.modules.add(mod);
		db.insert(epd);

		UsageTests.expectOne(result, epd);
		db.delete(epd);
		UsageTests.expectEmpty(result);
	}

}
