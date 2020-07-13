package org.openlca.core.database.descriptors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;

public class FlowDescriptorTest {

	private final IDatabase database = Tests.getDb();
	private final FlowDao flowDao = new FlowDao(database);

	private FlowProperty property;
	private Flow flow;

	@Before
	public void setUp() {
		property = new FlowProperty();
		property = new FlowPropertyDao(database).insert(property);
		flow = new Flow();
		flow.referenceFlowProperty = property;
		flow = flowDao.insert(flow);
	}

	@After
	public void tearDown() {
		flowDao.delete(flow);
		new FlowPropertyDao(database).delete(property);
	}

	@Test
	public void testGetRefFlowPropertyId() {
		var d = flowDao.getDescriptor(flow.id);
		Assert.assertEquals(property.id, d.refFlowPropertyId);
	}

}
