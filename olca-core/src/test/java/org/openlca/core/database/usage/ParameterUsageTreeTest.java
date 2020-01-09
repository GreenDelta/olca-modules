package org.openlca.core.database.usage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Objects;

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

public class ParameterUsageTreeTest {

	@Test
	public void testEmpty() {
		ParameterUsageTree tree = ParameterUsageTree.build(
				"this_param_does_not_exist", Tests.getDb());
		assertEquals("this_param_does_not_exist", tree.param);
		assertTrue(tree.nodes.isEmpty());
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
