package org.openlca.io.olca;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;

import java.io.IOException;

import static org.junit.Assert.*;

public class CategoryTest {

	private final TestContext ctx = TestContext.get();

	@Before
	public void setup() {
		var cats = new CategoryDao(ctx.source());
		var units = UnitGroup.of("U", "kg");
		units.category = cats.sync(ModelType.UNIT_GROUP, "unit", "groups");
		var mass = FlowProperty.of("V", units);
		mass.category = cats.sync(ModelType.FLOW_PROPERTY, "flow", "properties");
		var flow = Flow.waste("W", mass);
		flow.category = cats.sync(ModelType.FLOW, "waste", "flows");
		ctx.source().insert(units, mass, flow);
		new DatabaseImport(ctx.source(), ctx.target()).run();
	}

	@After
	public void cleanup() throws IOException {
		ctx.clear();
	}

	@Test
	public void testCategories() {
		var units = ctx.target().getForName(UnitGroup.class, "U");
		assertEquals("unit/groups", units.category.toPath());
		var mass = ctx.target().getForName(FlowProperty.class, "V");
		assertEquals("flow/properties", mass.category.toPath());
		var flow = ctx.target().getForName(Flow.class, "W");
		assertEquals("waste/flows", flow.category.toPath());
	}

}
