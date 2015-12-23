package org.openlca.core.database.references;

import org.openlca.core.Tests;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;

public class FlowReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return ModelType.FLOW;
	}

	@Override
	protected Flow createModel() {
		Flow flow = new Flow();
		flow.setCategory(insertAndAddExpected("category", new Category()));
		flow.setLocation(insertAndAddExpected("location", new Location()));
		flow.getFlowPropertyFactors().add(createFlowPropertyFactor());
		flow.getFlowPropertyFactors().add(createFlowPropertyFactor());
		flow = Tests.insert(flow);
		for (FlowPropertyFactor f : flow.getFlowPropertyFactors())
			addExpected("flowProperty", f.getFlowProperty(),
					"flowPropertyFactors", FlowPropertyFactor.class, f.getId());
		return flow;
	}

	private FlowPropertyFactor createFlowPropertyFactor() {
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(Tests.insert(new FlowProperty()));
		return factor;
	}

}
