package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.matrix.ImpactIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

import gnu.trove.set.hash.TLongHashSet;

/**
 * `BaseResult` is a common (abstract) super class of different result
 * implementations. It mainly contains the indices that map objects from the
 * database to the rows and columns of the result matrices.
 */
public abstract class BaseResult implements IResult {

	/**
	 * The index $\mathit{Idx}_A$ of the technology matrix $\mathbf{A}$. It maps the
	 * process-product pairs (or process-waste pairs) $\mathit{P}$ of the product
	 * system to the respective $n$ rows and columns of $\mathbf{A}$. If the product
	 * system contains other product systems as sub-systems, these systems are
	 * handled like processes and are also mapped as pair with their quantitative
	 * reference flow to that index (and also their processes etc.).
	 * <p>
	 * $$\mathit{Idx}_A: \mathit{P} \mapsto [0 \dots n-1]$$
	 */
	public TechIndex techIndex;

	/**
	 * The row index $\mathit{Idx}_B$ of the intervention matrix $\mathbf{B}$. It
	 * maps the (elementary) flows $\mathit{F}$ of the processes in the product
	 * system to the $k$ rows of $\mathbf{B}$.
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
	public ImpactIndex impactIndex;

	// cached descriptor lists which are initialized lazily
	private ArrayList<IndexFlow> _flows;
	private ArrayList<ImpactDescriptor> _impacts;
	private ArrayList<ProcessProduct> _products;
	private ArrayList<CategorizedDescriptor> _processes;

	@Override
	public boolean hasImpactResults() {
		return impactIndex != null && !impactIndex.isEmpty();
	}

	@Override
	public boolean hasFlowResults() {
		return flowIndex != null && !flowIndex.isEmpty();
	}

	@Override
	public final List<IndexFlow> getFlows() {
		if (_flows != null)
			return _flows;
		if (flowIndex == null)
			return Collections.emptyList();
		_flows = new ArrayList<>();
		flowIndex.each(_flows::add);
		return _flows;
	}

	@Override
	public final List<ImpactDescriptor> getImpacts() {
		if (_impacts != null)
			return _impacts;
		if (impactIndex == null)
			return Collections.emptyList();
		_impacts = new ArrayList<>();
		_impacts.addAll(impactIndex.content());
		return _impacts;
	}

	/**
	 * Get the process-product pairs (or process-waste pairs) $\mathit{P}$ of the
	 * product system. If the product system contains other product systems as
	 * sub-systems, these systems are handled like processes and are also included
	 * as pairs with their quantitative reference flow.
	 */
	public final List<ProcessProduct> getProviders() {
		if (_products != null)
			return _products;
		if (techIndex == null)
			return Collections.emptyList();
		_products = new ArrayList<>();
		_products.addAll(techIndex.content());
		return _products;
	}

	@Override
	public final List<CategorizedDescriptor> getProcesses() {
		if (_processes != null)
			return _processes;
		if (techIndex == null)
			return Collections.emptyList();
		_processes = new ArrayList<>();
		TLongHashSet handled = new TLongHashSet();
		for (ProcessProduct product : getProviders()) {
			CategorizedDescriptor process = product.process;
			if (process == null || handled.contains(process.id))
				continue;
			_processes.add(process);
			handled.add(process.id);
		}
		return _processes;
	}

	/**
	 * Switches the sign for input-flows.
	 */
	double adopt(IndexFlow flow, double value) {
		if (flow == null || !flow.isInput)
			return value;
		// avoid -0 in the results
		return value == 0 ? 0 : -value;
	}

}
