package org.openlca.core.results.solutions;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.TechIndex;

public interface SolutionProvider {

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

	/**
	 * Get the scaling vector of the product system for one unit of output (input)
	 * of product (waste flow) j. This is equivalent with the jth column of the
	 * inverse technology matrix $A^{-1}[:,j]$ in case of full in-memory matrices.
	 */
	double[] solutionOfOne(int product);

	boolean hasFlows();

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
	 * Get the scaled value $s_j * b_{ij}$ of the intervention matrix $B$. This
	 * is the direct contribution of process $j$ to the result of flow $i$.
	 */
	default double scaledValueOfB(int row, int col) {
		var s = scalingVector();
		var bij = valueOfB(row, col);
		return s[col] * bij;
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
	double totalFlowResultOfOne(int flow, int product);

	boolean hasImpacts();

	double[] totalImpacts();

	double[] totalImpactsOfOne(int product);

	double totalImpactOfOne(int indicator, int product);

	boolean hasCosts();

	double totalCosts();

	double totalCostsOfOne(int product);

	double loopFactorOf(int product);
}
