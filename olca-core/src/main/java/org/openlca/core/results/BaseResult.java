package org.openlca.core.results;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.TechIndex;

public abstract class BaseResult {

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
	 * The impact category index maps IDs of impact categories to column and row
	 * indices of the matrices and vectors of the mathematical model.
	 */
	public LongIndex impactIndex;

	/**
	 * Indicates whether the result contains cost results or not.
	 */
	public boolean hasCostResults = false;

	/**
	 * Returns true if this result contains an LCIA result.
	 */
	public boolean hasImpactResults() {
		return impactIndex != null && !impactIndex.isEmpty();
	}

}
