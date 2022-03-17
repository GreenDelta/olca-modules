package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

import gnu.trove.set.hash.TLongHashSet;

/**
 * `BaseResult` is a common (abstract) super class of different result
 * implementations. It mainly contains the indices that map objects from the
 * database to the rows and columns of the result matrices.
 */
public abstract class BaseResult implements IResult {

	// cached descriptor lists which are initialized lazily
	private ArrayList<EnviFlow> _flows;
	private ArrayList<ImpactDescriptor> _impacts;
	private ArrayList<TechFlow> _products;
	private ArrayList<RootDescriptor> _processes;


	@Override
	public final List<EnviFlow> getFlows() {
		if (_flows != null)
			return _flows;
		var flowIndex = enviIndex();
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
		var impactIndex = impactIndex();
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
	public final List<TechFlow> getProviders() {
		if (_products != null)
			return _products;
		var techIndex = techIndex();
		if (techIndex == null)
			return Collections.emptyList();
		_products = new ArrayList<>();
		_products.addAll(techIndex.content());
		return _products;
	}

	@Override
	public final List<RootDescriptor> getProcesses() {
		if (_processes != null)
			return _processes;
		var techIndex = techIndex();
		if (techIndex == null)
			return Collections.emptyList();
		_processes = new ArrayList<>();
		TLongHashSet handled = new TLongHashSet();
		for (TechFlow product : getProviders()) {
			RootDescriptor process = product.provider();
			if (process == null || handled.contains(process.id))
				continue;
			_processes.add(process);
			handled.add(process.id);
		}
		return _processes;
	}

}
