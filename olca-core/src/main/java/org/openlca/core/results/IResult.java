package org.openlca.core.results;

import java.util.List;

import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * The common protocol of all result types.
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
	 * Get the descriptors of the processes of the inventory model. If a product
	 * system contains other product systems, these sub-systems are also handled
	 * like processes and returned.
	 */
	List<CategorizedDescriptor> getProcesses();

	/**
	 * Returns the list of flows that are mapped to the row index of the
	 * intervention matrix. Typically, these are the elementary flows of the LCA
	 * model. The returned list is part of the result and should be never modified
	 * but reordering via sorting is allowed.
	 */
	List<IndexFlow> getFlows();

	/**
	 * Get the LCIA categories of the LCIA result.
	 */
	List<ImpactCategoryDescriptor> getImpacts();

}
