package org.openlca.core.database.references;

import org.openlca.core.Tests;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class SocialIndicatorReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return ModelType.SOCIAL_INDICATOR;
	}

	@Override
	protected SocialIndicator createModel() {
		SocialIndicator indicator = new SocialIndicator();
		indicator.setCategory(insertAndAddExpected("category", new Category()));
		indicator.activityQuantity = insertAndAddExpected("activityQuantity",
				new FlowProperty());
		UnitGroup group = new UnitGroup();
		Unit unit = new Unit();
		group.getUnits().add(unit);
		indicator.activityUnit = unit;
		group = Tests.insert(group);
		addExpected("activityUnit", unit);
		return Tests.insert(indicator);
	}

}
