package org.openlca.core.results;

import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;

/**
 * The common protocol of all result types.
 */
public interface IResult {

	/**
	 * Returns the demand for which this result was calculated.
	 */
	Demand demand();

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
	TechIndex techIndex();

	/**
	 * The row index $\mathit{Idx}_B$ of the intervention matrix $\mathbf{B}$. It
	 * maps the (elementary) flows $\mathit{F}$ of the processes in the product
	 * system to the $k$ rows of $\mathbf{B}$.
	 * <p>
	 * $$\mathit{Idx}_B: \mathit{F} \mapsto [0 \dots k-1]$$
	 */
	EnviIndex enviIndex();

	/**
	 * The row index $\mathit{Idx}_C$ of the matrix with the characterization
	 * factors $\mathbf{C}$. It maps the LCIA categories $\mathit{C}$ to the $l$
	 * rows of $\mathbf{C}$.
	 * <p>
	 * $$\mathit{Idx}_C: \mathit{C} \mapsto [0 \dots l-1]$$
	 */
	ImpactIndex impactIndex();

	/**
	 * Returns true when this result contains results for environmental flows.
	 */
	default boolean hasEnviFlows() {
		var enviIndex = enviIndex();
		return enviIndex != null && !enviIndex.isEmpty();
	}

	/**
	 * Returns true when this result contains LCIA results.
	 */
	default boolean hasImpacts() {
		var impactIndex = impactIndex();
		return impactIndex != null && !impactIndex.isEmpty();
	}

	/**
	 * Returns true when this result contains LCC results.
	 */
	boolean hasCosts();

}
