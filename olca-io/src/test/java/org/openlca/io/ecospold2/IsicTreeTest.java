package org.openlca.io.ecospold2;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.io.ecospold2.IsicTree.IsicNode;

public class IsicTreeTest {

	private IsicTree tree;

	@Before
	public void setUp() {
		tree = IsicTree.fromFile(this.getClass().getResourceAsStream(
				"isic_codes_rev4.txt"));
	}

	@Test
	public void testFindNode() {
		IsicNode node = tree.findNode("9492");
		Assert.assertEquals("Activities of political organizations",
				node.getName());
		Assert.assertEquals("949", node.getParent().getCode());
		Assert.assertEquals("94", node.getParent().getParent().getCode());
		Assert.assertEquals("S", node.getParent().getParent().getParent()
				.getCode());
	}
}
