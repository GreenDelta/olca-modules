package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.TransDeps;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.RootDescriptor;

import java.util.List;

import static org.junit.Assert.*;

public class ResultDepTest {

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
	public void testNoDependencies() {
		var deps = TransDeps.of(result, db);
		assertDeps(deps, result, product, mass, units);
	}

	@Test
	public void testProcessAndSystemDependencies() {
		var q = Flow.product("q", mass);
		var Q = Process.of("Q", q);
		var input = Q.input(product, 1.0);
		input.defaultProviderId = result.id;
		input.defaultProviderType = ProviderType.RESULT;
		db.insert(q, Q);

		var sys = ProductSystem.of("sys", Q);
		sys.link(TechFlow.of(result), Q);
		db.insert(sys);

		// process Q depends on the result
		var qDeps = TransDeps.of(Q, db);
		assertDeps(qDeps, q, result, product, mass, units);

		// system depends on Q which depends on the result
		var sysDeps = TransDeps.of(sys, db);
		assertDeps(sysDeps, sys, Q, q, result, product, mass, units);
	}

	@Test
	public void testEpdDependencies() {
		var epd = Epd.of("EPD", product);
		var mod = EpdModule.of("mod", result);
		epd.modules.add(mod);
		db.insert(epd);
		var epdDeps = TransDeps.of(epd, db);
		assertDeps(epdDeps, epd, result, product, mass, units);
		db.delete(epd);
	}

	private void assertDeps(List<RootDescriptor> deps, RootEntity... expected) {
		assertEquals( expected.length, deps.size());
		for (var e : expected) {
			var d = Descriptor.of(e);
			assertTrue(
					"could not find entity in dependencies: " + e,
					deps.contains(d));
		}
	}

}
