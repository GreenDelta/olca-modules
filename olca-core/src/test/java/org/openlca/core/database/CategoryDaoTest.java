package org.openlca.core.database;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class CategoryDaoTest {

	private CategoryDao dao = new CategoryDao(Tests.getDb());

	@Test
	public void testSimple() {
		Category category = dao.insert(create());
		Tests.emptyCache();
		Category alias = dao.getForId(category.getId());
		Assert.assertEquals(category.getName(), alias.getName());
		dao.delete(category); // non-attached
		alias = dao.getForId(category.getId());
		Assert.assertNull(alias);
	}

	@Test
	public void testAddChild() {
		Category parent = dao.insert(create());
		Category child = create();
		parent.getChildCategories().add(child);
		child.setCategory(parent);
		parent = dao.update(parent);
		child = parent.getChildCategories().get(0);
		Tests.emptyCache();
		Category alias = dao.getForId(parent.getId());
		Assert.assertEquals(1, alias.getChildCategories().size());
		Assert.assertEquals(child.getRefId(), alias.getChildCategories().get(0)
				.getRefId());
		dao.delete(alias);
		Assert.assertNull(dao.getForId(child.getId()));
	}

	@Test
	public void testFindRoot() {
		Category parent = create();
		Category child = create();
		parent.getChildCategories().add(child);
		child.setCategory(parent);
		dao.insert(parent);
		Tests.emptyCache();
		List<Category> roots = dao.getRootCategories(ModelType.FLOW);
		Assert.assertTrue(roots.contains(parent));
		Assert.assertFalse(roots.contains(child));
		dao.delete(parent);
	}

	@Test
	public void findAllRootTypes() {
		// in the openLCA application not all of these types are really used
		// in categories, but this test should work
		for (ModelType type : ModelType.values()) {
			Category cat = create();
			cat.setModelType(type);
			dao.insert(cat);
			Tests.emptyCache();
			List<Category> categories = dao.getRootCategories(type);
			Assert.assertTrue(categories.contains(cat));
			dao.delete(cat);
			categories = dao.getRootCategories(type);
			Assert.assertFalse(categories.contains(cat));
		}
	}

	private Category create() {
		Category category = new Category();
		category.setName("name");
		category.setModelType(ModelType.FLOW);
		return category;
	}
}
