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
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

public class UnitGroupUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<UnitGroupDescriptor> search;
	private UnitGroup group;

	@Before
	public void setup() {
		group = new UnitGroup();
		group.name = "group";
		group = new UnitGroupDao(database).insert(group);
		search = IUseSearch.FACTORY.createFor(ModelType.UNIT_GROUP, database);
	}

	@After
	public void tearDown() {
		new UnitGroupDao(database).delete(group);
	}

	@Test
	public void testFindNoUsage() {
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(group));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInFlowProperties() {
		FlowProperty property = createFlowProperty();
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(group));
		new FlowPropertyDao(database).delete(property);
		Descriptor expected = Descriptor.of(property);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private FlowProperty createFlowProperty() {
		FlowProperty property = new FlowProperty();
		property.name = "property";
		property.unitGroup = group;
		return new FlowPropertyDao(database).insert(property);
	}

}
