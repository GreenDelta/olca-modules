package org.openlca.core.database;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.ListUtils;
import org.openlca.core.TestSession;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

import com.google.common.base.Optional;

public class ProductSystemDaoTest {

	private ProductSystemDao dao;
	private ProductSystem productSystem;

	@Before
	public void setUp() throws Exception {
		dao = new ProductSystemDao(TestSession.getDefaultDatabase()
				.getEntityFactory());
		productSystem = createProductSystem();
		dao.insert(productSystem);
		TestSession.emptyCache();
	}

	@After
	public void tearDown() throws Exception {
		dao.delete(productSystem);
	}

	private ProductSystem createProductSystem() {
		ProductSystem productSystem = new ProductSystem();
		productSystem.setId(UUID.randomUUID().toString());
		productSystem.setName("name");
		return productSystem;
	}

	@Test
	public void testGetDescriptorsForNullCategory() throws Exception {
		Category cat = null;
		List<ProductSystemDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(cat));
		ProductSystemDescriptor descriptor = ListUtils.findDescriptor(
				productSystem.getId(), descriptors);
		Assert.assertNotNull(descriptor);
	}

	@Test
	public void testGetDescriptorsForCategory() throws Exception {
		Category category = addCategory();
		List<ProductSystemDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(category));
		ProductSystemDescriptor descriptor = ListUtils.findDescriptor(
				productSystem.getId(), descriptors);
		Assert.assertNotNull(descriptor);
		TestSession.getDefaultDatabase().createDao(Category.class)
				.delete(category);
	}

	private Category addCategory() throws Exception {
		Category category = new Category();
		category.setId(UUID.randomUUID().toString());
		category.setName("test_category");
		category.setModelType(ModelType.PRODUCT_SYSTEM);
		BaseDao<Category> catDao = TestSession.getDefaultDatabase().createDao(
				Category.class);
		catDao.insert(category);
		productSystem.setCategory(category);
		dao.update(productSystem);
		return category;
	}

}
