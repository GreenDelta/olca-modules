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
				node.name);
		Assert.assertEquals("949", node.parent.code);
		Assert.assertEquals("94", node.parent.parent.code);
		Assert.assertEquals("S", node.parent.parent.parent.code);
	}

	@Test
	public void testSyncFlowCategories() {
		IDatabase database = Tests.getDb();
		CategoryDao dao = new CategoryDao(database);
		Category cat = new Category();
		cat.name = "0121:Growing of grapes";
		cat.modelType = ModelType.FLOW;
		cat = dao.insert(cat);
		new IsicCategoryTreeSync(database, ModelType.FLOW).run();
		cat = dao.getForId(cat.id);
		Assert.assertNotNull(cat.category);
		Assert.assertEquals("012:Growing of perennial crops", cat.category.name);
	}

	@Test
	public void testSyncProcessCategories() {
		IDatabase database = Tests.getDb();
		CategoryDao dao = new CategoryDao(database);
		Category cat = new Category();
		String catName = "01:Crop and animal production, hunting and related service activities";
		cat.name = catName;
		cat.modelType = ModelType.PROCESS;
		cat = dao.insert(cat);
		new IsicCategoryTreeSync(database, ModelType.PROCESS).run();
		cat = dao.getForId(cat.id);
		Assert.assertNotNull(cat.category);
		Assert.assertEquals("A:Agriculture, forestry and fishing", cat.category.name);
	}

}
