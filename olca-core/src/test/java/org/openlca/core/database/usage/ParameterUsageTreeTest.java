package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.usage.ParameterUsageTree.Node;
import org.openlca.core.database.usage.ParameterUsageTree.UsageType;
import org.openlca.core.model.Process;
import org.openlca.core.model.*;
import org.openlca.core.model.descriptors.Descriptor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.*;

public class ParameterUsageTreeTest {

	private final IDatabase db = Tests.getDb();
	private Process process;
	private List<RootEntity> entities;

	@Before
	public void setup() {

		// a dependent global parameter
		var globalDep = new Parameter();
		globalDep.name = "global_dep_param";
		globalDep.isInputParameter = false;
		globalDep.scope = ParameterScope.GLOBAL;
		globalDep.formula = "param / pi";
		db.insert(globalDep);

		var flow = new Flow();
		flow.name = "flow";
		db.insert(flow);

		// local process parameter
		process = new Process();
		process.name = "process";
		var processParam = new Parameter();
		processParam.name = "param";
		processParam.isInputParameter = true;
		processParam.scope = ParameterScope.PROCESS;
		process.parameters.add(processParam);
		var processDepParam = new Parameter();
		processDepParam.name = "process_dep_param";
		processDepParam.formula = "21 * param";
		processDepParam.scope = ParameterScope.PROCESS;
		process.parameters.add(processDepParam);
		var exchange = process.output(flow, 1.0);
		exchange.formula = "sin(param)";
		db.insert(process);

		entities = List.of(process, globalDep, flow);
	}

	@After
	public void tearDown() {
		for (var e : entities) {
			db.delete(e);
		}
	}

	@Test
	public void testEmpty() {
		var tree = ParameterUsageTree.of(
				"this_param_does_not_exist", Tests.getDb());
		assertEquals("this_param_does_not_exist", tree.param);
		assertTrue(tree.nodes.isEmpty());
	}

	@Test
	public void findGlobalsByName() {
		var param = db.insert(Parameter.global("param", 42));
		var tree = ParameterUsageTree.of("param", db);
		var dep = find(tree, "global_dep_param");
		assertNotNull(dep);
		assertEquals(UsageType.FORMULA, dep.usageType);
		var global = find(tree, "param");
		assertNotNull(global);
		assertEquals(UsageType.DEFINITION, global.usageType);
		db.delete(param);
	}

	@Test
	public void findGlobalContext() {
		var global = db.insert(Parameter.global("param", 42));
		var tree = ParameterUsageTree.of(global, db);

		// no global definition
		assertNull(find(tree, "param"));

		// global dependent parameter
		var dep = find(tree, "global_dep_param");
		assertNotNull(dep);
		assertEquals(UsageType.FORMULA, dep.usageType);

		// no local parameters
		assertNull(find(tree, "process", "param"));
		assertNull(find(tree, "process", "process_dep_param"));
		assertNull(find(tree, "process", "flow"));
		db.delete(global);
	}

	@Test
	public void findProcessParametersByName() {
		var tree = ParameterUsageTree.of("param", db);

		var def = find(tree, "process", "param");
		assertNotNull(def);
		assertEquals(UsageType.DEFINITION, def.usageType);

		var dep = find(tree, "process", "process_dep_param");
		assertNotNull(dep);
		assertEquals(UsageType.FORMULA, dep.usageType);
	}

	@Test
	public void findProcessContext() {
		var param = process.parameters.stream()
				.filter(p -> "param".equals(p.name))
				.findFirst()
				.orElse(null);
		var tree = ParameterUsageTree.of(
				param, Descriptor.of(process), db);

		// process parameters
		var def = find(tree, "process", "param");
		assertNull(def); // No definition

		var dep = find(tree, "process", "process_dep_param");
		assertNotNull(dep);
		assertEquals(UsageType.FORMULA, dep.usageType);

		var exchange = find(tree, "process", "flow");
		assertNotNull(exchange);
		assertEquals(UsageType.FORMULA, exchange.usageType);

		// exclude others
		assertNull(find(tree, "param"));
		assertNull(find(tree, "global_dep_param"));
	}

	@Test
	public void testNoLocalUsage() {
		var process = TestProcess
				.refProduct("product", 1, "kg")
				.param("param", 42)
				.get();
		var tree = ParameterUsageTree.of(
				process.parameters.get(0),
				Descriptor.of(process),
				db);
		assertTrue(tree.isEmpty());
	}

	@Test
	public void testFindInAllocationFactors() {
		var global = db.insert(Parameter.global("param", 42));
		var process = TestProcess
				.refProduct("prod", 1, "kg")
				.prodIn("prod2", 0.5, "kg")
				.elemOut("CO2", 1.0, "kg")
				.alloc("prod", AllocationMethod.PHYSICAL, "1 / param")
				.get();

		var tree = ParameterUsageTree.of(global, db);
		var alloc = find(tree, process.name, "*");
		assertNotNull(alloc);
		assertEquals(UsageType.FORMULA, alloc.usageType);
		assertEquals("1 / param", alloc.usage);
		db.delete(global);
	}

	@Test
	public void testFindLocalInAllocationFactors() {
		var global = db.insert(Parameter.global("param", 42));
		var process = TestProcess
				.refProduct("prod", 1, "kg")
				.param("param", 42)
				.prodIn("prod2", 0.5, "kg")
				.elemOut("CO2", 1.0, "kg")
				.alloc("prod", AllocationMethod.PHYSICAL, "1 / param")
				.get();

		// search for global should not have a result
		var globalTree = ParameterUsageTree.of(global, db);
		var alloc = find(globalTree, process.name, "*");
		assertNull(alloc);

		var local = process.parameters.get(0);
		var localTree = ParameterUsageTree.of(
				local, Descriptor.of(process), db);
		alloc = find(localTree, process.name, "*");
		assertNotNull(alloc);
		assertEquals(UsageType.FORMULA, alloc.usageType);
		assertEquals("1 / param", alloc.usage);
		db.delete(global);
	}

	@Test
	public void testFindInCostFormula() {
		var units = UnitGroup.of("mass units", "kg");
		var mass = FlowProperty.of("mass", units);
		var param = Parameter.global("cost_param", 21);
		var p = Flow.product("p", mass);
		var process = Process.of("P", p);
		process.quantitativeReference.costFormula = "2 * cost_param";
		db.insert(units, mass, param, p, process);

		var tree = ParameterUsageTree.of("cost_param", db);
		var root = tree.nodes.stream()
				.filter(n -> Objects.equals(n.model, Descriptor.of(process)))
				.findAny()
				.orElseThrow();
		assertEquals(1, root.childs.size());
		var child = root.childs.get(0);
		assertEquals(Descriptor.of(p), child.model);

		db.delete(process, p, param, mass, units);
	}

	@Test
	public void testImpactFactor() {
		var global = db.insert(Parameter.global("param", 42));

		var flow = new Flow();
		flow.name = "CH4";
		db.insert(flow);

		var impact = new ImpactCategory();
		impact.name = "GWP";
		var factor = new ImpactFactor();
		factor.flow = flow;
		factor.value = 24.0;
		factor.formula = "2 * param";
		impact.impactFactors.add(factor);
		db.insert(impact);

		// find the factor node
		var tree = ParameterUsageTree.of("param", db);
		var node = find(tree, "GWP", "CH4");
		assertNotNull(node);
		assertEquals(Descriptor.of(flow), node.model);
		assertEquals(UsageType.FORMULA, node.usageType);
		assertEquals("2 * param", node.usage);

		// find the global parameter definition
		node = find(tree, "param");
		assertNotNull(node);
		assertEquals(Descriptor.of(global), node.model);
		assertEquals(UsageType.DEFINITION, node.usageType);
		db.delete(global, impact, flow);
	}


	private Node find(ParameterUsageTree tree, String... names) {
		var stream = tree.nodes.stream();
		Optional<Node> node = Optional.empty();
		for (var name : names) {
			node = name.equals("*")
					? stream.findAny()
					: stream.filter(n -> name.equals(n.name)).findAny();
			if (node.isEmpty())
				return null;
			stream = node.get().childs.stream();
		}
		return node.orElse(null);
	}
}
