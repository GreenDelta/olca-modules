package org.openlca.core.database.references;

import org.openlca.core.Tests;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;

public class UnitGroupReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return ModelType.UNIT_GROUP;
	}

	@Override
	protected UnitGroup createModel() {
		UnitGroup group = new UnitGroup();
		group.setCategory(insertAndAddExpected("category", new Category()));
		group.setDefaultFlowProperty(insertAndAddExpected(
				"defaultFlowProperty", new FlowProperty()));
		return Tests.insert(group);
	}

}
