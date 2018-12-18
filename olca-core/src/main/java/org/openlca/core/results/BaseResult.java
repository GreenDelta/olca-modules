package org.openlca.core.results;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

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
	 * 
	 * $$\mathit{Idx}_A: \mathit{P} \mapsto [0 \dots n-1]$$
	 */
	public TechIndex techIndex;

	/**
	 * The row index $\mathit{Idx}_B$ of the intervention matrix $\mathbf{B}$.
	 * It maps the (elementary) flows $\mathit{F}$ of the processes in the
	 * product system to the $k$ rows of $\mathbf{B}$.
	 * 
	 * $$\mathit{Idx}_B: \mathit{F} \mapsto [0 \dots k-1]$$
	 */
	public FlowIndex flowIndex;

	/**
	 * The row index $\mathit{Idx}_C$ of the matrix with the characterization
	 * factors $\mathbf{C}$. It maps the LCIA categories $\mathit{C}$ to the $l$
	 * rows of $\mathbf{C}$.
	 * 
	 * $$\mathit{Idx}_C: \mathit{C} \mapsto [0 \dots l-1]$$
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

	@Override
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

	/** Switches the sign for input-flows. */
	double adopt(FlowDescriptor flow, double value) {
		if (value == 0)
			return 0; // avoid -0 in the results
		return flowIndex.isInput(flow) ? -value : value;
	}

}
