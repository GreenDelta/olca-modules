package org.openlca.core.database;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.TestSession;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class CategoryDaoTest {

	private CategoryDao dao = new CategoryDao(TestSession.getDefaultDatabase()
			.getEntityFactory());

	@Test
	public void testSimple() throws Exception {
		Category category = create();
		dao.insert(category);
		TestSession.emptyCache();
		Category alias = dao.getForId(category.getId());
		Assert.assertEquals(category.getName(), alias.getName());
		dao.delete(category); // non-attached
		alias = dao.getForId(category.getId());
		Assert.assertNull(alias);
	}

	@Test
	public void testAddChild() throws Exception {
		Category parent = create();
		dao.insert(parent);
		Category child = create();
		parent.add(child);
		child.setParentCategory(parent);
		dao.update(parent);
		TestSession.emptyCache();
		Category alias = dao.getForId(parent.getId());
		Assert.assertEquals(1, alias.getChildCategories().length);
		Assert.assertEquals(child.getId(),
				alias.getChildCategories()[0].getId());
		dao.delete(alias);
		Assert.assertNull(dao.getForId(child.getId()));
	}

	@Test
	public void testFindRoot() throws Exception {
		Category parent = create();
		Category child = create();
		parent.add(child);
		child.setParentCategory(parent);
		dao.insert(parent);
		TestSession.emptyCache();
		List<Category> roots = dao.getRootCategories(ModelType.FLOW);
		Assert.assertTrue(roots.contains(parent));
		Assert.assertFalse(roots.contains(child));
		dao.delete(parent);
	}

	private Category create() {
		Category category = new Category();
		category.setId(UUID.randomUUID().toString());
		category.setName("name");
		category.setModelType(ModelType.FLOW);
		return category;
	}
}
