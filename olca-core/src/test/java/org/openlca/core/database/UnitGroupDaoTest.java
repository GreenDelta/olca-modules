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
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

import com.google.common.base.Optional;

public class UnitGroupDaoTest {

	private UnitGroupDao dao;
	private UnitGroup unitGroup;

	@Before
	public void setUp() throws Exception {
		dao = new UnitGroupDao(TestSession.getDefaultDatabase()
				.getEntityFactory());
		unitGroup = createUnitGroup();
		dao.insert(unitGroup);
		TestSession.emptyCache();
	}

	@After
	public void tearDown() throws Exception {
		dao.delete(unitGroup);
	}

	private UnitGroup createUnitGroup() {
		UnitGroup unitGroup = new UnitGroup();
		unitGroup.setId(UUID.randomUUID().toString());
		unitGroup.setName("name");
		return unitGroup;
	}

	@Test
	public void testGetDescriptorsForNullCategory() throws Exception {
		Category cat = null;
		List<UnitGroupDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(cat));
		UnitGroupDescriptor descriptor = ListUtils.findDescriptor(
				unitGroup.getId(), descriptors);
		Assert.assertNotNull(descriptor);
	}

	@Test
	public void testGetDescriptorsForCategory() throws Exception {
		Category category = addCategory();
		List<UnitGroupDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(category));
		UnitGroupDescriptor descriptor = ListUtils.findDescriptor(
				unitGroup.getId(), descriptors);
		Assert.assertNotNull(descriptor);
		TestSession.getDefaultDatabase().createDao(Category.class)
				.delete(category);
	}

	private Category addCategory() throws Exception {
		Category category = new Category();
		category.setId(UUID.randomUUID().toString());
		category.setName("test_category");
		category.setModelType(ModelType.FLOW_PROPERTY);
		BaseDao<Category> catDao = TestSession.getDefaultDatabase().createDao(
				Category.class);
		catDao.insert(category);
		unitGroup.setCategory(category);
		dao.update(unitGroup);
		return category;
	}

}
