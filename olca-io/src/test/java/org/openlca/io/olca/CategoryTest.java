package org.openlca.io.olca;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.Tests;

public class CategoryTest {

	private final IDatabase source = Tests.getDb();
	private final IDatabase target = Derby.createInMemory();

	@Before
	public void setup() {
		source.clear();
		var cats = new CategoryDao(source);
		var units = UnitGroup.of("U", "kg");
		units.category = cats.sync(ModelType.UNIT_GROUP, "unit", "groups");
		var mass = FlowProperty.of("V", units);
		mass.category = cats.sync(ModelType.FLOW_PROPERTY, "flow", "properties");
		var flow = Flow.waste("W", mass);
		flow.category = cats.sync(ModelType.FLOW, "waste", "flows");
		source.insert(units, mass, flow);
		new DatabaseImport(source, target).run();
	}

	@After
	public void cleanup() throws IOException {
		source.clear();
		target.close();
	}

	@Test
	public void testCategories() {
		var units = target.getForName(UnitGroup.class, "U");
		assertEquals("unit/groups", units.category.toPath());
		var mass = target.getForName(FlowProperty.class, "V");
		assertEquals("flow/properties", mass.category.toPath());
		var flow = target.getForName(Flow.class, "W");
		assertEquals("waste/flows", flow.category.toPath());
	}

}
