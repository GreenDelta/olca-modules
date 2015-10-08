package org.openlca.io.ecospold2.input;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.io.Tests;
import org.openlca.io.ecospold2.input.IsicTree.IsicNode;

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

	@Test
	public void testSyncFlowCategories() {
		IDatabase database = Tests.getDb();
		CategoryDao dao = new CategoryDao(database);
		Category cat = new Category();
		cat.setName("0121:Growing of grapes");
		cat.setModelType(ModelType.FLOW);
		cat = dao.insert(cat);
		new IsicCategoryTreeSync(database, ModelType.FLOW).run();
		cat = dao.getForId(cat.getId());
		Assert.assertNotNull(cat.getCategory());
		Assert.assertEquals("012:Growing of perennial crops", cat
				.getCategory().getName());
	}

	@Test
	public void testSyncProcessCategories() {
		IDatabase database = Tests.getDb();
		CategoryDao dao = new CategoryDao(database);
		Category cat = new Category();
		String catName = "01:Crop and animal production, hunting and related service activities";
		cat.setName(catName);
		cat.setModelType(ModelType.PROCESS);
		cat = dao.insert(cat);
		new IsicCategoryTreeSync(database, ModelType.PROCESS).run();
		cat = dao.getForId(cat.getId());
		Assert.assertNotNull(cat.getCategory());
		Assert.assertEquals("A:Agriculture, forestry and fishing", cat
				.getCategory().getName());
	}

}
