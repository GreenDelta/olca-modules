package org.openlca.core.results.solutions;

import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public interface SolutionProvider {

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
	FlowIndex flowIndex();

	/**
	 * The row index $\mathit{Idx}_C$ of the matrix with the characterization
	 * factors $\mathbf{C}$. It maps the LCIA categories $\mathit{C}$ to the $l$
	 * rows of $\mathbf{C}$.
	 * <p>
	 * $$\mathit{Idx}_C: \mathit{C} \mapsto [0 \dots l-1]$$
	 */
	DIndex<ImpactCategoryDescriptor> impactIndex();

	/**
	 * The scaling vector $\mathbf{s}$ which is calculated by solving the
	 * equation
	 * <p>
	 * $$\mathbf{A} \ \mathbf{s} = \mathbf{f}$$
	 * <p>
	 * where $\mathbf{A}$ is the technology matrix and $\mathbf{f}$ the final
	 * demand vector of the product system.
	 */
	double[] scalingVector();

	default double scalingFactorOf(int product) {
		if (product < 0)
			return 0;
		var s = scalingVector();
		return s == null || s.length < product
				? 0
				: s[product];
	}

	/**
	 * The total requirements of the products to fulfill the demand of the
	 * product system. As our technology matrix $\mathbf{A}$ is indexed
	 * symmetrically (means rows and columns refer to the same process-product
	 * pair) our product amounts are on the diagonal of the technology matrix
	 * $\mathbf{A}$ and the total requirements can be calculated by the
	 * following equation where $\mathbf{s}$ is the scaling vector ($\odot$
	 * denotes element-wise multiplication):
	 * <p>
	 * $$\mathbf{t} = \text{diag}(\mathbf{A}) \odot \mathbf{s}$$
	 */
	default double[] totalRequirements() {
		var index = techIndex();
		var t = new double[index.size()];
		for (int i = 0; i < t.length; i++) {
			t[i] = scaledValueOfA(i, i);
		}
		return t;
	}

	default double totalRequirementsOf(int product) {
		if (product < 0)
			return 0;
		var t = totalRequirements();
		return t == null || t.length < product
				? 0
				: t[product];

	}

	/**
	 * Get the unscaled column $j$ from the technology matrix $A$.
	 */
	double[] columnOfA(int j);

	/**
	 * Get the unscaled value $a_{ij}$ from the technology matrix $A$.
	 */
	default double valueOfA(int row, int col) {
		double[] column = columnOfA(col);
		return column[row];
	}

	/**
	 * Get the scaled value $s_j * a_{ij}$ of the technology matrix $A$. On
	 * the diagonal of $A$ these are the total requirements of the system.
	 */
	default double scaledValueOfA(int row, int col) {
		var s = scalingVector();
		var aij = valueOfA(row, col);
		return s[col] * aij;
	}

	/**
	 * Get the scaling vector of the product system for one unit of output (input)
	 * of product (waste flow) j. This is equivalent with the jth column of the
	 * inverse technology matrix $A^{-1}[:,j]$ in case of full in-memory matrices.
	 */
	double[] solutionOfOne(int product);

	/**
	 * Get the unscaled column $j$ from the intervention matrix $B$.
	 */
	double[] columnOfB(int j);

	/**
	 * Get the unscaled value $b_{ij}$ from the intervention matrix $B$.
	 */
	default double valueOfB(int row, int col) {
		double[] column = columnOfB(col);
		return column[row];
	}

	/**
	 * The inventory result $\mathbf{g}$ of the product system:
	 * <p>
	 * $$\mathbf{g} = \mathbf{B} \ \mathbf{s}$$
	 * <p>
	 * Where $\mathbf{B}$ is the intervention matrix and $\mathbf{s}$ the
	 * scaling vector. Note that inputs have negative values in this vector.
	 */
	double[] totalFlowResults();

	/**
	 * Get the the direct result of the given flow and product related to the
	 * final demand of the system. This is basically the element $g_{ij}$
	 * of the column-wise scaled intervention matrix $B$:
	 * <p>
	 * $$ G = B \text{diag}(s) $$
	 */
	default double directFlowResult(int flow, int product) {
		var s = scalingVector();
		if (s == null || s.length <= product)
			return 0;
		return s[product] * valueOfB(flow, product);
	}

	/**
	 * Returns the total flow results (direct + upstream) related to one unit of
	 * the given product $j$ in the system. This is the respective column $j$
	 * of the intensity matrix $M$:
	 * <p>
	 * $$M = B * A^{-1}$$
	 */
	double[] totalFlowResultsOfOne(int product);

	/**
	 * Returns the total result (direct + upstream) of the given flow related
	 * to one unit of the given product in the system.
	 */
	default double totalFlowResultOfOne(int flow, int product) {
		var column = totalFlowResultsOfOne(product);
		if (column == null || column.length <= flow)
			return 0;
		return column[flow];
	}

	/**
	 * Returns the total flow result (direct + upstream) of the given flow
	 * and product related to the final demand of the system.
	 */
	default double totalFlowResult(int flow, int product) {
		if (flow < 0 || product < 0)
			return 0;
		double[] tr = totalRequirements();
		if (tr == null || tr.length <= product)
			return 0;
		double[] ofOne = totalFlowResultsOfOne(product);
		if (ofOne == null || ofOne.length < flow)
			return 0;
		double loop = loopFactorOf(product);
		return loop * tr[product] * ofOne[flow];
	}

	double[] totalImpacts();

	double directImpact(int indicator, int product);

	double[] totalImpactsOfOne(int product);

	default double totalImpactOfOne(int indicator, int product) {
		var ofOne = totalImpactsOfOne(product);
		if (ofOne == null || ofOne.length <= indicator)
			return 0;
		return ofOne[indicator];
	}

	double totalCosts();

	double totalCostsOfOne(int product);

	double loopFactorOf(int product);
}
