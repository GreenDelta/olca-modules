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
		flow.category = insertAndAddExpected("category", new Category());
		flow.location = insertAndAddExpected("location", new Location());
		flow.flowPropertyFactors.add(createFlowPropertyFactor());
		flow.flowPropertyFactors.add(createFlowPropertyFactor());
		flow = Tests.insert(flow);
		for (FlowPropertyFactor f : flow.flowPropertyFactors)
			addExpected("flowProperty", f.flowProperty,
					"flowPropertyFactors", FlowPropertyFactor.class, f.id);
		return flow;
	}

	private FlowPropertyFactor createFlowPropertyFactor() {
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.flowProperty = Tests.insert(new FlowProperty());
		return factor;
	}

}
