package org.openlca.core.database.usage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.core.Tests;

public class ParameterUsageTreeTest {

	@Test
	public void testEmpty() {
		ParameterUsageTree tree = ParameterUsageTree.build(
				"this_param_does_not_exist", Tests.getDb());
		assertEquals("this_param_does_not_exist", tree.param);
		assertTrue(tree.nodes.isEmpty());
	}
}
