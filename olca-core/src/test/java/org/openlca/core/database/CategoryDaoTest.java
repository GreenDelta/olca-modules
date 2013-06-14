package org.openlca.core.database;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.TestSession;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;

public class CategoryDaoTest {

	private CategoryDao dao = new CategoryDao(TestSession.getMySQLDatabase()
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
	public void addChild() throws Exception {
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

	private Category create() {
		Category category = new Category();
		category.setId(UUID.randomUUID().toString());
		category.setName("name");
		category.setComponentClass(Flow.class.getCanonicalName());
		return category;
	}

}
