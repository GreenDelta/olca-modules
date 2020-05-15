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
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.database.usage.ParameterUsageTree.Node;
import org.openlca.core.database.usage.ParameterUsageTree.UsageType;

public class ParameterUsageTreeTest {

	private final IDatabase db = Tests.getDb();
	private Parameter global;

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

	}

	@After
	public void tearDown() {
		Tests.clearDb();
	}

	@Test
	public void testEmpty() {
		ParameterUsageTree tree = ParameterUsageTree.build(
				"this_param_does_not_exist", Tests.getDb());
		assertEquals("this_param_does_not_exist", tree.param);
		assertTrue(tree.nodes.isEmpty());
	}

	@Test
	public void findGlobalsByName() {
		var tree = ParameterUsageTree.build("param", db);

		var dep = findRoot("global_dep_param", tree);
		Assert.assertTrue(dep.isPresent());
		Assert.assertEquals(UsageType.FORMULA, dep.get().type);

		var global = findRoot("param", tree);
		Assert.assertTrue(global.isPresent());
		Assert.assertEquals(UsageType.DEFINITION, global.get().type);
	}

	private Optional<Node> findRoot(String name, ParameterUsageTree tree) {
		return tree.nodes.stream()
				.filter(n -> {
					if (n.context == null)
						return false;
					return name.equals(n.context.name);
				}).findAny();
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

		ParameterUsageTree tree = ParameterUsageTree.build(
				"param", db);
		Arrays.asList(impact, flow, param).forEach(
				e -> Tests.delete(e));

		// there should be now a parameter definition node
		// and an LCIA category node in the tree
		assertFalse(tree.nodes.isEmpty());
		assertEquals("param", tree.param);

		boolean found = false;
		ImpactCategoryDescriptor d = Descriptors.toDescriptor(impact);
		for (ParameterUsageTree.Node node : tree.nodes) {
			if (Objects.equals(d, node.context)) {
				found = true;
				assertEquals(Descriptors.toDescriptor(flow),
						node.childs.get(0).context);
			}
		}
		assertTrue(found);
	}
}
