package org.openlca.core.results;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.RegFlowIndex;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

/**
 * `BaseResult` is a common (abstract) super class of different result
 * implementations. It mainly contains the indices that map objects from the
 * database to the rows and columns of the result matrices.
 */
public abstract class BaseResult implements IResult {

	/**
	 * The index $\mathit{Idx}_A$ of the technology matrix $\mathbf{A}$. It maps
	 * the process-product pairs (or process-waste pairs) $\mathit{P}$ of the
	 * product system to the respective $n$ rows and columns of $\mathbf{A}$. If
	 * the product system contains other product systems as sub-systems, these
	 * systems are handled like processes and are also mapped as pair with their
	 * quantitative reference flow to that index (and also their processes
	 * etc.).
	 * <p>
	 * $$\mathit{Idx}_A: \mathit{P} \mapsto [0 \dots n-1]$$
	 */
	public TechIndex techIndex;

	/**
	 * The row index $\mathit{Idx}_B$ of the intervention matrix $\mathbf{B}$.
	 * It maps the (elementary) flows $\mathit{F}$ of the processes in the
	 * product system to the $k$ rows of $\mathbf{B}$.
	 * <p>
	 * $$\mathit{Idx}_B: \mathit{F} \mapsto [0 \dots k-1]$$
	 */
	public FlowIndex flowIndex;

	/**
	 * The row index $\mathit{Idx}_C$ of the matrix with the characterization
	 * factors $\mathbf{C}$. It maps the LCIA categories $\mathit{C}$ to the $l$
	 * rows of $\mathbf{C}$.
	 * <p>
	 * $$\mathit{Idx}_C: \mathit{C} \mapsto [0 \dots l-1]$$
	 */
	public DIndex<ImpactCategoryDescriptor> impactIndex;

	/**
	 * A regionalized flow index in case of a regionalized calculation. If the
	 * regionalized flow index is not null the flow index must be null and the
	 * other way around.
	 */
	public RegFlowIndex regFlowIndex;

	@Override
	public boolean hasImpactResults() {
		return impactIndex != null && !impactIndex.isEmpty();
	}

	@Override
	public boolean hasFlowResults() {
		if (flowIndex != null) {
			return !flowIndex.isEmpty();
		}
		return regFlowIndex != null && !regFlowIndex.isEmpty();
	}

	@Override
	public boolean isInput(FlowDescriptor flow) {
		if (flowIndex != null)
			return flowIndex.isInput(flow);
		return regFlowIndex != null && regFlowIndex.isInput(flow);
	}

	@Override
	public Set<FlowDescriptor> getFlows() {
		if (flowIndex != null)
			return flowIndex.content();
		if (regFlowIndex != null)
			return regFlowIndex.getFlows();
		return Collections.emptySet();
	}

	@Override
	public Set<ImpactCategoryDescriptor> getImpacts() {
		if (impactIndex == null)
			return Collections.emptySet();
		return impactIndex.content();
	}

	/**
	 * If this result is regionalized this method returns the descriptors of
	 * the different locations in this result.
	 */
	public Set<LocationDescriptor> getLocations() {
		if (regFlowIndex != null)
			return regFlowIndex.getLocations();
		return Collections.emptySet();
	}

	public boolean isRegionalized() {
		return regFlowIndex != null;
	}

	/**
	 * Get the process-product pairs (or process-waste pairs) $\mathit{P}$ of
	 * the product system. If the product system contains other product systems
	 * as sub-systems, these systems are handled like processes and are also
	 * included as pairs with their quantitative reference flow.
	 */
	public Set<ProcessProduct> getProviders() {
		if (techIndex == null)
			return Collections.emptySet();
		return techIndex.content();
	}

	@Override
	public Set<CategorizedDescriptor> getProcesses() {
		return getProviders().stream()
				.map(p -> p.process)
				.collect(Collectors.toSet());
	}

	/**
	 * Switches the sign for input-flows.
	 */
	double adopt(FlowDescriptor flow, double value) {
		if (value == 0)
			return 0; // avoid -0 in the results
		return flowIndex.isInput(flow) ? -value : value;
	}

}
