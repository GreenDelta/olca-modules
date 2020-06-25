package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

public class FlowPropertyUseSearchTest {

	private IDatabase db = Tests.getDb();

	private FlowProperty unused;
	private FlowProperty used;
	private Flow flow;
	private UnitGroup unitGroup;

	@Before
	public void setUp() {
		UnitGroupDao groupDao = new UnitGroupDao(db);
		FlowPropertyDao propDao = new FlowPropertyDao(db);
		FlowDao flowDao = new FlowDao(db);
		unused = propDao.insert(new FlowProperty());
		used = propDao.insert(new FlowProperty());
		unitGroup = new UnitGroup();
		unitGroup.defaultFlowProperty = used;
		unitGroup = groupDao.insert(unitGroup);
		flow = new Flow();
		FlowPropertyFactor fac = new FlowPropertyFactor();
		fac.flowProperty = used;
		flow.flowPropertyFactors.add(fac);
		flow = flowDao.insert(flow);
	}

	@After
	public void tearDown() {
		UnitGroupDao groupDao = new UnitGroupDao(db);
		FlowPropertyDao propDao = new FlowPropertyDao(db);
		FlowDao flowDao = new FlowDao(db);
		flowDao.delete(flow);
		groupDao.delete(unitGroup);
		propDao.delete(unused);
		propDao.delete(used);
	}

	@Test
	public void testUnused() {
		IUseSearch<CategorizedDescriptor> search = IUseSearch.FACTORY.createFor(
				ModelType.FLOW_PROPERTY, db);
		List<CategorizedDescriptor> list = search.findUses(
				Descriptor.of(unused));
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testUsed() {
		IUseSearch<CategorizedDescriptor> search = IUseSearch.FACTORY.createFor(
				ModelType.FLOW_PROPERTY, db);
		List<CategorizedDescriptor> list = search.findUses(
				Descriptor.of(used));
		Assert.assertEquals(2, list.size());
		for (Descriptor d : list) {
			if (d.type != ModelType.UNIT_GROUP) {
				Assert.assertEquals(ModelType.FLOW, d.type);
				Assert.assertEquals(flow.id, d.id);
			} else {
				Assert.assertEquals(unitGroup.id, d.id);
			}
		}
	}
}
