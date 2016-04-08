package org.openlca.core.database.references;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.Tests;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;

public class FlowPropertyFactorReferenceSearchTest extends
		BaseReferenceSearchTest {

	private Map<Long, Class<? extends AbstractEntity>> ownerTypes = new HashMap<>();
	private Map<Long, Long> ownerIds = new HashMap<>();

	@Override
	protected ModelType getModelType() {
		return null;
	}

	@Override
	protected Class<? extends AbstractEntity> getModelClass() {
		return FlowPropertyFactor.class;
	}

	@Override
	protected boolean isNestedSearchTest() {
		return true;
	}

	@Override
	protected List<Reference> findReferences(Set<Long> ids) {
		return new FlowPropertyFactorReferenceSearch(Tests.getDb(), ownerTypes,
				ownerIds).findReferences(ids);
	}

	@Override
	protected FlowPropertyFactor createModel() {
		Flow flow = new Flow();
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(Tests.insert(new FlowProperty()));
		flow.getFlowPropertyFactors().add(factor);
		flow = Tests.insert(flow);
		factor = flow.getFactor(factor.getFlowProperty());
		ownerIds.put(factor.getId(), flow.getId());
		ownerTypes.put(factor.getId(), Flow.class);
		addExpected(new Reference("flowProperty", FlowProperty.class, factor
				.getFlowProperty().getId(), Flow.class, flow.getId(),
				"flowPropertyFactors", FlowPropertyFactor.class,
				factor.getId(), false));
		return factor;
	}
}
