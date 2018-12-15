package org.openlca.core.results;

import java.util.Set;

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
	 * Get the (elementary) flows of the inventory result.
	 */
	Set<FlowDescriptor> getFlows();

	/**
	 * Get the LCIA categories of the LCIA result.
	 */
	Set<ImpactCategoryDescriptor> getImpacts();

}
