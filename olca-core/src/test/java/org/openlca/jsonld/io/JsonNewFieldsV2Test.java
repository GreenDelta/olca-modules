package org.openlca.jsonld.io;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

import java.util.Objects;
import java.util.function.Consumer;

public class JsonNewFieldsV2Test {

	private final IDatabase db = Tests.getDb();
	private Process process;

	@Before
	public void setup() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var product = Flow.product("product", mass);
		process = Process.of("process", product);
		db.insert(units, mass, product, process);
	}

	@After
	public void cleanup() {
		db.clear();
	}

	@Test
	public void testLibraryAndTags() {
		process.library = "some-lib";
		process.tags = "some,tags";
		process = db.update(process);
		var store = withExport(export -> export.write(process));
		db.delete(process);
		new JsonImport(store, db).run();
		var copy = db.get(Process.class, process.refId);
		assertEquals("some,tags", copy.tags);
		assertNull(copy.library);
	}

	@Test
	public void testFlowLocations() {
		var mass = process.quantitativeReference.flow.referenceFlowProperty;
		var flow = db.insert(Flow.elementary("F", mass));
		var location = db.insert(Location.of("Loc"));

		// for exchanges
		var exchange = process.output(flow, 2);
		exchange.location = location;
		process = db.update(process);

		// for impact factors
		var impact = ImpactCategory.of("Ind");
		impact.factor(flow, 10).location = location;
		db.insert(impact);

		var store = withExport(export -> {
			export.write(process);
			export.write(impact);
		});
		db.clear();
		new JsonImport(store, db).run();

		var processCopy = db.get(Process.class, process.refId);
		var exchangeCopy = processCopy.exchanges.stream()
			.filter(e -> Objects.equals(flow.refId, e.flow.refId))
			.findFirst()
			.orElseThrow();
		assertEquals("Loc", exchangeCopy.location.name);

		var impactCopy = db.get(ImpactCategory.class, impact.refId);
		var factorCopy = impactCopy.impactFactors.stream()
			.filter(f -> Objects.equals(flow.refId, f.flow.refId))
			.findFirst()
			.orElseThrow();
		assertEquals("Loc", factorCopy.location.name);
		db.clear();
	}

	@Test
	public void testAllocationFormula() {
		var product = process.quantitativeReference.flow;
		var economic = AllocationFactor.economic(product, 1);
		economic.formula = "1 + 0";
		process.allocationFactors.add(economic);
		var physical = AllocationFactor.physical(product, 1);
		physical.formula = "1 * 1";
		process.allocationFactors.add(physical);
		process = db.update(process);

		var store = withExport(export -> export.write(process));
		db.delete(process);
		new JsonImport(store, db).run();

		var processCopy = db.get(Process.class, process.refId);
		var formula1 = processCopy.allocationFactors.stream()
			.filter(a -> a.method == AllocationMethod.ECONOMIC)
			.findFirst()
			.orElseThrow()
			.formula;
		assertEquals("1 + 0", formula1);

		var formula2 = processCopy.allocationFactors.stream()
			.filter(a -> a.method == AllocationMethod.PHYSICAL)
			.findFirst()
			.orElseThrow()
			.formula;
		assertEquals("1 * 1", formula2);
	}

	@Test
	public void testInlinedNwSets() {
		var impact = db.insert(ImpactCategory.of("impact"));
		var method = ImpactMethod.of("method");
		method.impactCategories.add(impact);
		var nwSet = NwSet.of("nw-set");
		nwSet.factors.add(NwFactor.of(impact, 100, 0.01));
		method.add(nwSet);
		db.insert(method);

		var store = withExport(export -> export.write(method));
		db.clear();
		new JsonImport(store, db).run();

		var methodCopy = db.get(ImpactMethod.class, method.refId);
		var nwSetCopy = methodCopy.nwSets.get(0);
		assertEquals("nw-set", nwSetCopy.name);
		var factor = nwSetCopy.factors.get(0);
		assertEquals(impact.refId, factor.impactCategory.refId);
		assertEquals(100, factor.normalisationFactor, 1e-10);
		assertEquals(0.01, factor.weightingFactor, 1e-10);
	}

	@Test
	public void testStandAloneImpacts() {
		var impact = db.insert(ImpactCategory.of("impact"));
		var method1 = ImpactMethod.of("method1");
		method1.impactCategories.add(impact);
		db.insert(method1);
		var method2 = ImpactMethod.of("method2");
		method2.impactCategories.add(impact);
		db.insert(method2);

		var store = withExport(export -> {
			export.write(method1);
			export.write(method2);
		});
		db.clear();
		new JsonImport(store, db).run();

		var m1 = db.get(ImpactMethod.class, method1.refId);
		var i1 = m1.impactCategories.get(0);
		var m2 = db.get(ImpactMethod.class, method2.refId);
		var i2 = m2.impactCategories.get(0);

		assertEquals(i1, i2);
		assertEquals(i1.id, i2.id);
		assertEquals(impact.refId, i1.refId);
	}

	@Test
	public void testProviderTypes() {
		var mass = process.quantitativeReference.flow.referenceFlowProperty;

		// products and inputs
		var prodA = db.insert(Flow.product("product a", mass));
		var prodB = db.insert(Flow.product("product b", mass));
		var prodC = db.insert(Flow.product("product c", mass));
		process.input(prodA, 1);
		process.input(prodB, 2);
		process.input(prodC, 3);
		process = db.update(process);

		// provider types
		var procA = db.insert(Process.of("process a", prodA));
		var procB = db.insert(Process.of("process b", prodB));
		var sysB = db.insert(ProductSystem.of("system b", procB));
		var resC = db.insert(Result.of("result c", prodC));

		// linked system
		var system = ProductSystem.of(process);
		system.link(TechFlow.of(procA), process);
		system.link(TechFlow.of(sysB), process);
		system.link(TechFlow.of(resC), process);

		var store = withExport(export -> export.write(system));
		db.clear();
		new JsonImport(store, db).run();

		// load products
		prodA = db.get(Flow.class, prodA.refId);
		assertNotNull(prodA);
		prodB = db.get(Flow.class, prodB.refId);
		assertNotNull(prodB);
		prodC = db.get(Flow.class, prodC.refId);
		assertNotNull(prodC);

		// reload providers
		procA = db.get(Process.class, procA.refId);
		assertNotNull(procA);
		sysB = db.get(ProductSystem.class, sysB.refId);
		assertNotNull(sysB);
		resC = db.get(Result.class, resC.refId);
		assertNotNull(resC);

		var systemCopy = db.get(ProductSystem.class, system.refId);

		// check providers in system
		boolean[] providerOk = {false, false, false};
		for (var provider : systemCopy.processes) {
			providerOk[0] = providerOk[0] || provider == procA.id;
			providerOk[1] = providerOk[1] || provider == sysB.id;
			providerOk[2] = providerOk[2] || provider == resC.id;
		}
		assertArrayEquals(new boolean[]{true, true, true}, providerOk);

		// check provider links
		boolean[] linksOk = {false, false, false};
		for (var link : systemCopy.processLinks) {
			linksOk[0] = linksOk[0] || (link.providerId == procA.id
				&& link.flowId == prodA.id && link.providerType == 0);
			linksOk[1] = linksOk[1] || (link.providerId == sysB.id
				&& link.flowId == prodB.id && link.providerType == 1);
			linksOk[2] = linksOk[2] || (link.providerId == resC.id
				&& link.flowId == prodC.id && link.providerType == 2);
		}
		assertArrayEquals(new boolean[]{true, true, true}, linksOk);

	}

	@Test
	public void testParameterRedefSets() {

		// create a product system with parameter sets
		var param = db.insert(Parameter.global("global", 42));
		var system = ProductSystem.of("system", process);

		var redef1 = ParameterRedef.of(param, 42);
		var set1 = ParameterRedefSet.of("baseline", redef1);
		set1.isBaseline = true;
		system.parameterSets.add(set1);

		var redef2 = ParameterRedef.of(param, 21);
		var set2 = ParameterRedefSet.of("alternative", redef2);
		system.parameterSets.add(set2);
		db.insert(system);

		// export and import the system
		var store = withExport(export -> export.write(system));
		db.clear();
		assertNull(db.get(ProductSystem.class, system.refId));
		new JsonImport(store, db).run();

		// check
		var copy = db.get(ProductSystem.class, system.refId);
		assertNotNull(copy);

		var s1 = copy.parameterSets.stream()
			.filter(s -> "baseline".equals(s.name))
			.findFirst()
			.orElseThrow();
		assertTrue(s1.isBaseline);
		assertEquals(1, s1.parameters.size());
		var rd1 = s1.parameters.get(0);
		assertEquals("global", rd1.name);
		assertEquals(42, rd1.value, 1e-10);

		var s2 = copy.parameterSets.stream()
			.filter(s -> "alternative".equals(s.name))
			.findFirst()
			.orElseThrow();
		assertFalse(s2.isBaseline);
		assertEquals(1, s2.parameters.size());
		var rd2 = s2.parameters.get(0);
		assertEquals("global", rd2.name);
		assertEquals(21, rd2.value, 1e-10);
	}

	private MemStore withExport(Consumer<JsonExport> fn) {
		var store = new MemStore();
		var export = new JsonExport(db, store)
			.withReferences(true)
			.skipLibraryData(false);
		fn.accept(export);
		return store;
	}
}
