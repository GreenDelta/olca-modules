package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

public class UnitGroupUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<UnitGroupDescriptor> search;
	private UnitGroup group;

	@Before
	public void setup() {
		group = new UnitGroup();
		group.setName("group");
		group = new UnitGroupDao(database).insert(group);
		search = IUseSearch.FACTORY.createFor(ModelType.UNIT_GROUP, database);
	}

	@After
	public void tearDown() {
		new UnitGroupDao(database).delete(group);
	}

	@Test
	public void testFindNoUsage() {
		List<CategorizedDescriptor> models = search.findUses(Descriptors
				.toDescriptor(group));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInFlowProperties() {
		FlowProperty property = createFlowProperty();
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(group));
		new FlowPropertyDao(database).delete(property);
		BaseDescriptor expected = Descriptors.toDescriptor(property);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private FlowProperty createFlowProperty() {
		FlowProperty property = new FlowProperty();
		property.setName("property");
		property.setUnitGroup(group);
		return new FlowPropertyDao(database).insert(property);
	}

}
