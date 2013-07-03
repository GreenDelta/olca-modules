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
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.FlowDescriptor;

import com.google.common.base.Optional;

public class FlowDaoTest {

	private FlowDao dao;
	private Flow flow;

	@Before
	public void setUp() throws Exception {
		dao = new FlowDao(TestSession.getDefaultDatabase().getEntityFactory());
		flow = createFlow();
		dao.insert(flow);
	}

	@After
	public void tearDown() throws Exception {
		dao.delete(flow);
	}

	private Flow createFlow() {
		Flow flow = new Flow();
		flow.setName("test_flow");
		flow.setRefId(UUID.randomUUID().toString());
		flow.setFlowType(FlowType.ELEMENTARY_FLOW);
		return flow;
	}

	@Test
	public void testGetDescriptorsForNullCategory() {
		Category cat = null;
		List<FlowDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(cat));
		FlowDescriptor descriptor = ListUtils.findDescriptor(flow.getRefId(),
				descriptors);
		Assert.assertNotNull(descriptor);
	}

	@Test
	public void testGetDescriptorsForCategory() throws Exception {
		Category category = addCategory();
		List<FlowDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(category));
		FlowDescriptor descriptor = ListUtils.findDescriptor(flow.getRefId(),
				descriptors);
		Assert.assertNotNull(descriptor);
		TestSession.getDefaultDatabase().createDao(Category.class)
				.delete(category);
	}

	private Category addCategory() throws Exception {
		Category category = new Category();
		category.setRefId(UUID.randomUUID().toString());
		category.setName("test_category");
		category.setModelType(ModelType.FLOW);
		BaseDao<Category> catDao = TestSession.getDefaultDatabase().createDao(
				Category.class);
		catDao.insert(category);
		flow.setCategory(category);
		dao.update(flow);
		return category;
	}

}
