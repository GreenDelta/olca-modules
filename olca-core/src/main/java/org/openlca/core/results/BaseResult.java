package org.openlca.core.results;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.Provider;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public abstract class BaseResult implements IResult {

	/**
	 * The index of the technology matrix that maps the process-flow pairs
	 * (products and waste flows) of the technosphere to the respective matrix
	 * indices.
	 */
	public TechIndex techIndex;

	/**
	 * The flow index which maps the flow-IDs from the interventions to column
	 * and row indices of the matrices and vectors of the mathematical model.
	 */
	public FlowIndex flowIndex;

	/**
	 * The impact category index maps impact categories (their descriptors) to
	 * column and row indices of the matrices and vectors of the mathematical
	 * model.
	 */
	public DIndex<ImpactCategoryDescriptor> impactIndex;

	@Override
	public boolean hasImpactResults() {
		return impactIndex != null && !impactIndex.isEmpty();
	}

	@Override
	public boolean hasFlowResults() {
		return flowIndex != null && !flowIndex.isEmpty();
	}

	/** Returns true when the given flow is an input flow. */
	public boolean isInput(FlowDescriptor flow) {
		if (flowIndex == null)
			return false;
		return flowIndex.isInput(flow);
	}

	@Override
	public Set<FlowDescriptor> getFlows() {
		if (flowIndex == null)
			return Collections.emptySet();
		return flowIndex.content();
	}

	@Override
	public Set<ImpactCategoryDescriptor> getImpacts() {
		if (impactIndex == null)
			return Collections.emptySet();
		return impactIndex.content();
	}

	/**
	 * Get the providers of the inventory model. These are the process-flow and
	 * product-system-flow pairs that are linked to processes in the product
	 * system and form the index of the technology matrix.
	 */
	public Set<Provider> getProviders() {
		if (techIndex == null)
			return Collections.emptySet();
		return techIndex.content();
	}

	/**
	 * Get the descriptors of the processes and product systems (the
	 * sub-systems) of the inventory model; the elements that provide a product
	 * output or a waste input (for treatment).
	 */
	public Set<CategorizedDescriptor> getProviderHosts() {
		return getProviders().stream()
				.map(p -> p.entity)
				.collect(Collectors.toSet());
	}

}
