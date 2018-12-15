package org.openlca.core.results;

import java.util.Set;

import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * The common protocoll of all result types.
 */
public interface IResult {

	/**
	 * Returns true when this result contains (elementary) flow results.
	 */
	boolean hasFlowResults();

	/**
	 * Returns true when this result contains LCIA results.
	 */
	boolean hasImpactResults();

	/**
	 * Returns true when this result contains LCC results.
	 */
	boolean hasCostResults();

	/**
	 * Get the descriptors of the processes and product systems (the
	 * sub-systems) of the inventory model; the elements that provide a product
	 * output or a waste input (for treatment).
	 */
	Set<CategorizedDescriptor> getProcesses();

	/**
	 * Get the (elementary) flows of the inventory result.
	 */
	Set<FlowDescriptor> getFlows();

	/**
	 * Indicates whether the given flow is handled as an input flow in the
	 * result.
	 */
	boolean isInput(FlowDescriptor flow);

	/**
	 * Get the LCIA categories of the LCIA result.
	 */
	Set<ImpactCategoryDescriptor> getImpacts();

}
