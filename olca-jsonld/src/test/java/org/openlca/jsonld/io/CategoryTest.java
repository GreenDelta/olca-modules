package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Category;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.Tests;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class CategoryTest extends AbstractZipTest {

	@Test
	public void testCategory() throws Exception {
		CategoryDao dao = new CategoryDao(Tests.getDb());
		Category category = createModel(dao);
		doExport(category, dao);
		doImport(dao, category);
		dao.delete(category);
	}

	private Category createModel(CategoryDao dao) {
		Category category = new Category();
		category.setName("category");
		category.setRefId(UUID.randomUUID().toString());
		dao.insert(category);
		return category;
	}

	private void doExport(Category category, CategoryDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(category);
		});
		dao.delete(category);
		Assert.assertFalse(dao.contains(category.getRefId()));
	}

	private void doImport(CategoryDao dao, Category category) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(category.getRefId()));
		Category clone = dao.getForRefId(category.getRefId());
		Assert.assertEquals(category.getName(), clone.getName());
	}
}
