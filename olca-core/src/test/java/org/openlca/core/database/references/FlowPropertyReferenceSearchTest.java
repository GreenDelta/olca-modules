package org.openlca.core.database.references;

import org.openlca.core.Tests;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;

public class FlowPropertyReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return ModelType.FLOW_PROPERTY;
	}

	@Override
	protected FlowProperty createModel() {
		FlowProperty property = new FlowProperty();
		property.setCategory(insertAndAddExpected("category", new Category()));
		property.setUnitGroup(insertAndAddExpected("unitGroup", new UnitGroup()));
		return Tests.insert(property);
	}
}
