package org.openlca.core.database.descriptors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class FlowDescriptorTest {

	private IDatabase database = Tests.getDb();
	private FlowDao flowDao = new FlowDao(database);

	private FlowProperty property;
	private Flow flow;

	@Before
	public void setUp() throws Exception {
		property = new FlowProperty();
		property = database.createDao(FlowProperty.class).insert(property);
		flow = new Flow();
		flow.setReferenceFlowProperty(property);
		flow = flowDao.insert(flow);
	}

	@After
	public void tearDown() throws Exception {
		flowDao.delete(flow);
		database.createDao(FlowProperty.class).delete(property);
	}

	@Test
	public void testGetRefFlowPropertyId() throws Exception {
		FlowDescriptor descriptor = flowDao.getDescriptor(flow.getId());
		Assert.assertEquals(property.getId(), descriptor.getRefFlowPropertyId());
	}

}
