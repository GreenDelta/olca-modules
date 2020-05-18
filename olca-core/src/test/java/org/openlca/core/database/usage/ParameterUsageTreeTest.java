package org.openlca.core.database.usage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.database.usage.ParameterUsageTree.Node;
import org.openlca.core.database.usage.ParameterUsageTree.UsageType;

public class ParameterUsageTreeTest {

	private final IDatabase db = Tests.getDb();
	private Parameter global;
	private Process process;

	@Before
	public void setup() {
		Tests.clearDb();

		global = new Parameter();
		global.name = "param";
		global.isInputParameter = true;
		global.scope = ParameterScope.GLOBAL;
		Tests.insert(global);

		// a dependent global parameter
		var globalDep = new Parameter();
		globalDep.name = "global_dep_param";
		globalDep.isInputParameter = false;
		globalDep.scope = ParameterScope.GLOBAL;
		globalDep.formula = "param / pi";
		Tests.insert(globalDep);

		var flow = new Flow();
		flow.name = "flow";
		Tests.insert(flow);

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
		var exchange = process.exchange(flow);
		exchange.formula = "sin(param)";
		Tests.insert(process);

	}

	@After
	public void tearDown() {
		Tests.clearDb();
	}

	@Test
	public void testEmpty() {
		ParameterUsageTree tree = ParameterUsageTree.of(
				"this_param_does_not_exist", Tests.getDb());
		assertEquals("this_param_does_not_exist", tree.param);
		assertTrue(tree.nodes.isEmpty());
	}

	@Test
	public void findGlobalsByName() {
		var tree = ParameterUsageTree.of("param", db);

		var dep = find(tree, "global_dep_param");
		Assert.assertNotNull(dep);
		Assert.assertEquals(UsageType.FORMULA, dep.usageType);

		var global = find(tree, "param");
		Assert.assertNotNull(global);
		Assert.assertEquals(UsageType.DEFINITION, global.usageType);
	}

	@Test
	public void findProcessParametersByName() {
		var tree = ParameterUsageTree.of("param", db);

		var def = find(tree, "process", "param");
		Assert.assertNotNull(def);
		Assert.assertEquals(UsageType.DEFINITION, def.usageType);

		var dep = find(tree, "process", "process_dep_param");
		Assert.assertNotNull(dep);
		Assert.assertEquals(UsageType.FORMULA, dep.usageType);
	}

	@Test
	public void findProcessContext() {
		var param = process.parameters.stream()
				.filter(p -> "param".equals(p.name))
				.findFirst()
				.orElse(null);
		var tree = ParameterUsageTree.of(
				param, Descriptors.toDescriptor(process), db);

		// process parameters
		var def = find(tree, "process", "param");
		Assert.assertNull(def); // No definition

		var dep = find(tree, "process", "process_dep_param");
		Assert.assertNotNull(dep);
		Assert.assertEquals(UsageType.FORMULA, dep.usageType);

		// exclude others
		Assert.assertNull(find(tree, "param"));
		Assert.assertNull(find(tree, "global_dep_param"));
	}

	private Node find(ParameterUsageTree tree, String...names) {
		var stream = tree.nodes.stream();
		Optional<Node> node = Optional.empty();
		for (var name : names) {
			node = stream.filter(n -> name.equals(n.name)).findAny();
			if (node.isEmpty())
				return null;
			stream = node.get().childs.stream();
		}
		return node.orElse(null);
	}

	@Test
	public void testImpactFactor() {
		IDatabase db = Tests.getDb();

		Parameter param = new Parameter();
		param.name = "param";
		param.isInputParameter = true;
		param.value = 12.0;
		param.scope = ParameterScope.GLOBAL;
		new ParameterDao(db).insert(param);

		Flow flow = new Flow();
		flow.name = "CH4";
		new FlowDao(db).insert(flow);

		ImpactCategory impact = new ImpactCategory();
		impact.name = "GWP";
		ImpactFactor i = new ImpactFactor();
		i.flow = flow;
		i.value = 24.0;
		i.formula = "2 * param";
		impact.impactFactors.add(i);
		new ImpactCategoryDao(db).insert(impact);

		ParameterUsageTree tree = ParameterUsageTree.of("param", db);
		Arrays.asList(impact, flow, param)
				.forEach(Tests::delete);

		// there should be now a parameter definition node
		// and an LCIA category node in the tree
		assertFalse(tree.nodes.isEmpty());
		assertEquals("param", tree.param);

		boolean found = false;
		ImpactCategoryDescriptor d = Descriptors.toDescriptor(impact);
		for (ParameterUsageTree.Node node : tree.nodes) {
			if (Objects.equals(d, node.model)) {
				found = true;
				assertEquals(Descriptors.toDescriptor(flow),
						node.childs.get(0).model);
			}
		}
		assertTrue(found);
	}
}
