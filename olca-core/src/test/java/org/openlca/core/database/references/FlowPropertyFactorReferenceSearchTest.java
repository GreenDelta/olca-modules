package org.openlca.core.database.references;

import java.util.Collections;
import java.util.List;

import org.openlca.core.Tests;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;

public class FlowPropertyFactorReferenceSearchTest extends
		BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return null;
	}

	@Override
	protected Class<?> getModelClass() {
		return FlowPropertyFactor.class;
	}

	@Override
	protected List<Reference> findReferences(long id) {
		return new FlowPropertyFactorReferenceSearch(Tests.getDb())
				.findReferences(Collections.singleton(id));
	}

	@Override
	protected FlowPropertyFactor createModel() {
		Flow flow = new Flow();
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(insertAndAddExpected(new FlowProperty()));
		flow.getFlowPropertyFactors().add(factor);
		flow = Tests.insert(flow);
		return flow.getFactor(factor.getFlowProperty());
	}
}
