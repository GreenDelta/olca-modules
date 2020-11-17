package org.openlca.core.database.references;

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
		property.category = insertAndAddExpected("category", new Category());
		property.unitGroup = insertAndAddExpected("unitGroup", new UnitGroup());
		return db.insert(property);
	}
}
