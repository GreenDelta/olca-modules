package org.openlca.core.database;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.TestSession;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class CategoryDaoTest {

	private CategoryDao dao = new CategoryDao(TestSession.getDefaultDatabase());

	@Test
	public void testSimple() {
		Category category = create();
		System.out.println(category.getId());
		dao.insert(category);
		System.out.println(category.getId());
		TestSession.emptyCache();
		Category alias = dao.getForId(category.getId());
		Assert.assertEquals(category.getName(), alias.getName());
		dao.delete(category); // non-attached
		alias = dao.getForId(category.getId());
		Assert.assertNull(alias);
	}

	@Test
	public void testAddChild() {
		Category parent = create();
		dao.insert(parent);
		Category child = create();
		parent.getChildCategories().add(child);
		child.setCategory(parent);
		dao.update(parent);
		TestSession.emptyCache();
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
		TestSession.emptyCache();
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
			TestSession.emptyCache();
			List<Category> categories = dao.getRootCategories(type);
			Assert.assertTrue(categories.contains(cat));
			dao.delete(cat);
			categories = dao.getRootCategories(type);
			Assert.assertFalse(categories.contains(cat));
		}
	}

	private Category create() {
		Category category = new Category();
		category.setRefId(UUID.randomUUID().toString());
		category.setName("name");
		category.setModelType(ModelType.FLOW);
		return category;
	}
}
