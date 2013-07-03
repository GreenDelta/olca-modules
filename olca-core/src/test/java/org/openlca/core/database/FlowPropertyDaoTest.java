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
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;

import com.google.common.base.Optional;

public class FlowPropertyDaoTest {

	private FlowPropertyDao dao;
	private FlowProperty flowProperty;

	@Before
	public void setUp() throws Exception {
		dao = new FlowPropertyDao(TestSession.getDefaultDatabase()
				.getEntityFactory());
		flowProperty = createFlowProperty();
		dao.insert(flowProperty);
		TestSession.emptyCache();
	}

	@After
	public void tearDown() throws Exception {
		dao.delete(flowProperty);
	}

	private FlowProperty createFlowProperty() {
		FlowProperty flowProperty = new FlowProperty();
		flowProperty.setRefId(UUID.randomUUID().toString());
		flowProperty.setName("name");
		return flowProperty;
	}

	@Test
	public void testGetDescriptorsForNullCategory() throws Exception {
		Category cat = null;
		List<FlowPropertyDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(cat));
		FlowPropertyDescriptor descriptor = ListUtils.findDescriptor(
				flowProperty.getRefId(), descriptors);
		Assert.assertNotNull(descriptor);
	}

	@Test
	public void testGetDescriptorsForCategory() throws Exception {
		Category category = addCategory();
		List<FlowPropertyDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(category));
		FlowPropertyDescriptor descriptor = ListUtils.findDescriptor(
				flowProperty.getRefId(), descriptors);
		Assert.assertNotNull(descriptor);
		TestSession.getDefaultDatabase().createDao(Category.class)
				.delete(category);
	}

	private Category addCategory() throws Exception {
		Category category = new Category();
		category.setRefId(UUID.randomUUID().toString());
		category.setName("test_category");
		category.setModelType(ModelType.FLOW_PROPERTY);
		BaseDao<Category> catDao = TestSession.getDefaultDatabase().createDao(
				Category.class);
		catDao.insert(category);
		flowProperty.setCategory(category);
		dao.update(flowProperty);
		return category;
	}

}
