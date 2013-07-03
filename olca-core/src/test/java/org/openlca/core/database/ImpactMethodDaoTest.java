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
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

import com.google.common.base.Optional;

public class ImpactMethodDaoTest {

	private ImpactMethodDao dao;
	private ImpactMethod impactMethod;

	@Before
	public void setUp() throws Exception {
		dao = new ImpactMethodDao(TestSession.getDefaultDatabase()
				.getEntityFactory());
		impactMethod = createImpactMethod();
		dao.insert(impactMethod);
		TestSession.emptyCache();
	}

	@After
	public void tearDown() throws Exception {
		dao.delete(impactMethod);
	}

	private ImpactMethod createImpactMethod() {
		ImpactMethod impactMethod = new ImpactMethod();
		impactMethod.setRefId(UUID.randomUUID().toString());
		impactMethod.setName("name");
		return impactMethod;
	}

	@Test
	public void testGetDescriptorsForNullCategory() throws Exception {
		Category cat = null;
		List<ImpactMethodDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(cat));
		ImpactMethodDescriptor descriptor = ListUtils.findDescriptor(
				impactMethod.getRefId(), descriptors);
		Assert.assertNotNull(descriptor);
	}

	@Test
	public void testGetDescriptorsForCategory() throws Exception {
		Category category = addCategory();
		List<ImpactMethodDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(category));
		ImpactMethodDescriptor descriptor = ListUtils.findDescriptor(
				impactMethod.getRefId(), descriptors);
		Assert.assertNotNull(descriptor);
		TestSession.getDefaultDatabase().createDao(Category.class)
				.delete(category);
	}

	private Category addCategory() throws Exception {
		Category category = new Category();
		category.setRefId(UUID.randomUUID().toString());
		category.setName("test_category");
		category.setModelType(ModelType.IMPACT_METHOD);
		BaseDao<Category> catDao = TestSession.getDefaultDatabase().createDao(
				Category.class);
		catDao.insert(category);
		impactMethod.setCategory(category);
		dao.update(impactMethod);
		return category;
	}
}
