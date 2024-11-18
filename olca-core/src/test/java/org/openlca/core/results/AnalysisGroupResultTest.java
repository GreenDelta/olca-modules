package org.openlca.core.results;

import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.linking.LinkingConfig;
import org.openlca.core.model.AnalysisGroup;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.results.agroups.AnalysisGroupResult;

public class AnalysisGroupResultTest {

	private final IDatabase db = Tests.getDb();
	private ImpactMethod method;
	private Flow elem;
	private FlowProperty mass;

	@Before
	public void setup() {
		var units = UnitGroup.of("Mass units", "kg");
		mass = FlowProperty.of("Mass", units);
		elem = Flow.elementary("e", mass);
		var i = ImpactCategory.of("I");
		i.factor(elem, 1);
		method = ImpactMethod.of("M");
		method.impactCategories.add(i);
		db.insert(units, mass, elem, i, method);
	}

	@After
	public void cleanup() {
		var entities = List.of(
			method,
			method.impactCategories.getFirst(),
			elem,
			mass,
			mass.unitGroup
		);
		for (var e : entities) {
			db.delete(e);
		}
	}

	/// ```
	///       +- #A1 -- P
	/// Root -+
	///       +- #A2 -- Q
	///```
	@Test
	public void testSimple() {
		var root = processOf("Root");
		var a1 = processOf("#A1");
		var a2 = processOf("#A2");
		var p = processOf("P");
		var q = processOf("Q");
		link(root, a1, 1);
		link(root, a2, 1);
		link(a1, p, 1);
		link(a2, q, 1);

		var system = systemOf(root);
		var result = resultOf(system);
		delete(system);

		check(result, Map.of(
			"A1", 2.0,
			"A2", 2.0,
			"Top", 1.0
		));
	}

	/// ```
	///
	///       +- P -- Q
	/// Root -+
	///       +- #A1 --- S
	///```
	@Test
	public void testTopBranch() {
		var root = processOf("Root");
		var p = processOf("P");
		link(root, p, 1);
		link(p, processOf("Q"), 1);
		var a1 = processOf("#A1");
		link(root, a1, 1);
		link(a1, processOf("S"), 1);

		var system = systemOf(root);
		var result = resultOf(system);
		delete(system);
		check(result, Map.of(
			"Top", 3.0,
			"A1", 2.0
		));
	}


	/// ```
	///       +- Q -- #B1 -- R -+
	/// Root -+                 +- #A1 -- S
	///       +- T -- #B2 -- U -+
	///```
	@Test
	public void testJointChain() {
		var root = processOf("Root");
		var q = processOf("Q");
		var b1 = processOf("#B1");
		var r = processOf("R");
		var a1 = processOf("#A1");
		var s = processOf("S");
		var t = processOf("T");
		var b2 = processOf("#B2");
		var u = processOf("U");

		link(root, q, 1);
		link(root, t, 1);
		link(q, b1, 1);
		link(t, b2, 1);
		link(b1, r, 1);
		link(b2, u, 1);
		link(r, a1, 1);
		link(u, a1, 0.5);
		link(a1, s, 1);

		var system = systemOf(root);
		var result = resultOf(system);
		delete(system);

		check(result, Map.of(
			"A1", 3.0,
			"B1", 2.0,
			"B2", 2.0,
			"Top", 3.0
		));
	}

	/// ```
	///                +-----------+
	/// Root -- #B1 -- P -- #A1 -- S
	///
	/// ```
	@Test
	public void testSimpleBranchLoop() {
		var root = processOf("Root");
		var b1 = processOf("#B1");
		var p = processOf("P");
		var a1 = processOf("#A1");
		var s = processOf("S");

		link(root, b1, 1);
		link(b1, p, 1);
		link(p, a1, 1);
		link(a1, s, 1);
		link(s, p, 0.5);

		var system = systemOf(root);
		var result = resultOf(system);
		delete(system);
		check(result, Map.of(
			"Top", 1.0,
			"B1", 2.0,
			"A1", 5.0
		));
	}

	private Process processOf(String name) {
		var product = db.insert(Flow.product(name, mass));
		var process = Process.of(name, product);
		process.output(elem, 1);
		return db.insert(process);
	}

	private void link(Process process, Process provider, double amount) {
		var p = db.get(Process.class, process.id);
		var product = provider.quantitativeReference.flow;
		p.input(product, amount);
		db.update(p);
	}

	private ProductSystem systemOf(Process root) {
		var system = new ProductSystemBuilder(
			MatrixCache.createLazy(db), new LinkingConfig()).build(root);
		var groups = new ArrayList<AnalysisGroup>();
		for (var pid : system.processes) {
			var p = db.get(Process.class, pid);
			var parts = p.name.split("#");
			if (parts.length < 2)
				continue;
			var label = parts[1].strip();
			var group = groups.stream()
					.filter(gi -> gi.name.equals(label))
					.findFirst()
					.orElse(null);
			if (group == null) {
				group = new AnalysisGroup();
				group.name = label;
				groups.add(group);
			}
			group.processes.add(pid);
		}
		system.analysisGroups.addAll(groups);
		return db.insert(system);
	}

	private void delete(ProductSystem system) {
		var deque = new ArrayDeque<RootEntity>();
		for (var pid : system.processes) {
			var p = db.get(Process.class, pid);
			// delete processes before flows
			deque.addFirst(p);
			deque.addLast(p.quantitativeReference.flow);
		}
		db.delete(system);
		for (var e : deque) {
			db.delete(e);
		}
	}

	private Map<String, Double> resultOf(ProductSystem system) {
		var setup = CalculationSetup.of(system)
			.withImpactMethod(method);
		var result = new SystemCalculator(db)
			.calculate(setup);
		var ar = AnalysisGroupResult.of(system, result);
		var impact = Descriptor.of(method.impactCategories.getFirst());
		return ar.getResultsOf(impact);
	}

	private void check(
		Map<String, Double> calculated, Map<String, Double> expected
	) {
		assertEquals(expected.keySet(), calculated.keySet());
		for (var key : expected.keySet()) {
			var ve = expected.get(key);
			var vc = calculated.get(key);
			assertNotNull(vc);
			assertEquals(ve, vc, 1e-15);
		}
	}
}
