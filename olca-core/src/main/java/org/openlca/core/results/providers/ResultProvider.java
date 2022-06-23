package org.openlca.core.results.providers;

import java.util.Arrays;

import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.MatrixIndex;
import org.openlca.core.matrix.index.TechIndex;

/**
 * Defines the general interface of a `ResultProvider`. The documentation is
 * based on matrix algebra but this does not mean that an implementation has
 * to be based on matrices. It just has to implement this protocol. A user of
 * a `ResultProvider` should treat the returned values as live views on the
 * data of the result provider and thus should **never** modify these values.
 * Because of this, the default implementations in this interface often have
 * copy-behaviour and it is often more efficient to overwrite them in a
 * specific implementation.
 */
public interface ResultProvider {

	double[] EMPTY_VECTOR = new double[0];

	/**
	 * The demand to which the result is related.
	 */
	Demand demand();

	/**
	 * The index $\mathit{Idx}_A$ of the technology matrix $\mathbf{A}$. It maps the
	 * process-product pairs (or process-waste pairs) $\mathit{P}$ of the product
	 * system to the respective $n$ rows and columns of $\mathbf{A}$. If the product
	 * system contains other product systems as sub-systems, these systems are
	 * handled like processes and are also mapped as pair with their quantitative
	 * reference flow to that index:
	 * <p>
	 * $$
	 * \mathit{Idx}_A: \mathit{P} \mapsto [0 \dots n-1]
	 * $$
	 */
	TechIndex techIndex();

	/**
	 * The row index $\mathit{Idx}_B$ of the intervention matrix $\mathbf{B}$. It
	 * maps the (elementary) flows $\mathit{F}$ of the processes in the product
	 * system to the $m$ rows of $\mathbf{B}$.
	 * <p>
	 * $$
	 * \mathit{Idx}_B: \mathit{F} \mapsto [0 \dots m-1]
	 * $$
	 */
	EnviIndex enviIndex();

	/**
	 * The row index $\mathit{Idx}_C$ of the matrix with the characterization
	 * factors $\mathbf{C}$ (the impact matrix). It maps the impact categories
	 * $\mathit{C}$ to the $k$ rows of $\mathbf{C}$.
	 * <p>
	 * $$
	 * \mathit{Idx}_C: \mathit{C} \mapsto [0 \dots k-1]
	 * $$
	 */
	ImpactIndex impactIndex();

	default boolean hasFlows() {
		return MatrixIndex.isPresent(enviIndex());
	}

	default boolean hasImpacts() {
		return MatrixIndex.isPresent(impactIndex());
	}

	boolean hasCosts();

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
	 * Returns the scaling factor $s_j$ for product $j$ from the scaling vector
	 * $\mathbf{s}$:
	 * <p>
	 * $$
	 * s_j = \mathbf{s}[j]
	 * $$
	 */
	default double scalingFactorOf(int product) {
		var s = scalingVector();
		return isEmpty(s)
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
	 * $$
	 * \mathbf{t} = \text{diag}(\mathbf{A}) \odot \mathbf{s}
	 * $$
	 */
	default double[] totalRequirements() {
		var index = techIndex();
		var t = new double[index.size()];
		for (int i = 0; i < t.length; i++) {
			t[i] = scaledTechValueOf(i, i);
		}
		return t;
	}

	default double totalRequirementsOf(int techFlow) {
		var t = totalRequirements();
		return isEmpty(t)
			? 0
			: t[techFlow];
	}

	/**
	 * Get the unscaled column $j$ from the technology matrix $A$.
	 */
	double[] techColumnOf(int techFlow);

	/**
	 * Get the unscaled value $a_{ij}$ from the technology matrix $A$.
	 */
	default double techValueOf(int row, int col) {
		double[] column = techColumnOf(col);
		return column[row];
	}

	/**
	 * Get the scaled value $s_j * a_{ij}$ of the technology matrix $A$. On
	 * the diagonal of $A$ these are the total requirements of the system.
	 */
	default double scaledTechValueOf(int row, int col) {
		return scalingFactorOf(col) * techValueOf(row, col);
	}

	/**
	 * Get the scaling vector of the product system for one unit of output (input)
	 * of product (waste flow) j. This is equivalent with the jth column of the
	 * inverse technology matrix $A^{-1}[:,j]$ in case of full in-memory matrices.
	 */
	double[] solutionOfOne(int techFlow);

	/**
	 * The loop factor $loop_j$ of a product $i$ is calculated via:
	 * <p>
	 * $$
	 * loop_j = \frac{1}{\mathbf{A}_{jj} \ \mathbf{A}^{-1}_{jj}}
	 * $$
	 * <p>
	 * It is $1.0$ if the process of the product is not in a loop. Otherwise
	 * it describes ...
	 */
	double loopFactorOf(int techFlow);

	default double totalFactorOf(int techFlow) {
		var t = totalRequirementsOf(techFlow);
		var loop = loopFactorOf(techFlow);
		return loop * t;
	}

	/**
	 * Get the unscaled column $j$ from the intervention matrix $B$.
	 */
	double[] unscaledFlowsOf(int techFlow);

	/**
	 * Get the unscaled value $b_{ij}$ from the intervention matrix $B$.
	 */
	default double unscaledFlowOf(int flow, int product) {
		double[] column = unscaledFlowsOf(product);
		return column[flow];
	}

	/**
	 * Returns the direct flow results for the given product {@code j}. This is
	 * the scaled column {@code j} from the intervention matrix {@code B}
	 * calculated via {@code s[j] * B[:, j]}.
	 *
	 * @param techFlow the product index {@code j >= 0} for which the direct flow
	 *                results should be returned.
	 */
	default double[] directFlowsOf(int techFlow) {
		var flows = unscaledFlowsOf(techFlow);
		var s = scalingVector();
		if (isEmpty(flows) || isEmpty(s))
			return EMPTY_VECTOR;
		return scale(flows, s[techFlow]);
	}

	/**
	 * Get the direct result of the given flow and product related to the
	 * final demand of the system. This is basically the element $g_{ij}$
	 * of the column-wise scaled intervention matrix $B$:
	 * <p>
	 * $$ G = B \text{diag}(s) $$
	 */
	default double directFlowOf(int flow, int techFlow) {
		return scalingFactorOf(techFlow) * unscaledFlowOf(flow, techFlow);
	}

	/**
	 * Returns the total flow results (direct + upstream) related to one unit of
	 * the given product $j$ in the system. This is the respective column $j$
	 * of the intensity matrix $M$:
	 * <p>
	 * $$M = B * A^{-1}$$
	 */
	double[] totalFlowsOfOne(int techFlow);

	/**
	 * Returns the total result (direct + upstream) of the given flow related
	 * to one unit of the given product in the system.
	 */
	default double totalFlowOfOne(int flow, int techFlow) {
		var totals = totalFlowsOfOne(techFlow);
		return isEmpty(totals)
			? 0
			: totals[flow];
	}

	default double[] totalFlowsOf(int techFlow) {
		var factor = totalFactorOf(techFlow);
		var totals = totalFlowsOfOne(techFlow);
		return scale(totals, factor);
	}

	/**
	 * Returns the total flow result (direct + upstream) of the given flow
	 * and product related to the final demand of the system.
	 */
	default double totalFlowOf(int flow, int techFlow) {
		return totalFactorOf(techFlow) * totalFlowOfOne(flow, techFlow);
	}

	/**
	 * The inventory result $\mathbf{g}$ of the product system:
	 * <p>
	 * $$\mathbf{g} = \mathbf{B} \ \mathbf{s}$$
	 * <p>
	 * Where $\mathbf{B}$ is the intervention matrix and $\mathbf{s}$ the
	 * scaling vector. Note that inputs have negative values in this vector.
	 */
	double[] totalFlows();

	/**
	 * Get the impact factors $c_m$ for the given flow $m$ which is the $m$th
	 * column of the impact matrix $C \in \mathbb{R}^{k \times m}$:
	 * <p>
	 * $$
	 * c_m = C[:, m]
	 * $$
	 */
	double[] impactFactorsOf(int flow);

	/**
	 * Get the impact factor $c_{km}$ for the given indicator $m$ and flow $m$
	 * which is the respective entry in the impact matrix
	 * $C \in \mathbb{R}^{k \times m}$:
	 * <p>
	 * $$
	 * c_{km} = C[k, m]
	 * $$
	 */
	default double impactFactorOf(int indicator, int flow) {
		var factors = impactFactorsOf(flow);
		return isEmpty(factors)
			? 0
			: factors[indicator];
	}

	/**
	 * A LCIA category * flow matrix that contains the direct contributions of the
	 * elementary flows to the LCIA result. This matrix can be calculated by
	 * column-wise scaling of the matrix with the characterization factors
	 * $\mathbf{C}$ with the inventory result $\mathbf{g}$:
	 * <p>
	 * $$\mathbf{H} = \mathbf{C} \ \text{diag}(\mathbf{g})$$
	 */
	default double[] flowImpactsOf(int flow) {
		var totals = totalFlows();
		var impacts = impactFactorsOf(flow);
		if (isEmpty(totals) || isEmpty(impacts))
			return EMPTY_VECTOR;
		return scale(impacts, totals[flow]);
	}

	default double flowImpactOf(int indicator, int flow) {
		var totals = totalFlows();
		var factor = impactFactorOf(indicator, flow);
		return isEmpty(totals)
			? 0
			: factor * totals[flow];
	}

	/**
	 * A LCIA category * process-product matrix that contains the direct
	 * contributions of the processes to the LCIA result. This can be calculated by
	 * a matrix-matrix multiplication of the direct inventory contributions
	 * $\mathbf{G}$ with the matrix with the characterization factors $\mathbf{C}$:
	 * <p>
	 * $$\mathbf{H} = \mathbf{C} \ \mathbf{G}$$
	 */
	double[] directImpactsOf(int techFlow);

	default double directImpactOf(int indicator, int techFlow) {
		var impacts = directImpactsOf(techFlow);
		return isEmpty(impacts)
			? 0
			: impacts[indicator];
	}

	double[] totalImpactsOfOne(int techFlow);

	default double totalImpactOfOne(int indicator, int techFlow) {
		var impacts = totalImpactsOfOne(techFlow);
		return isEmpty(impacts)
			? 0
			: impacts[indicator];
	}

	default double[] totalImpactsOf(int techFlow) {
		var impacts = totalImpactsOfOne(techFlow);
		var factor = totalFactorOf(techFlow);
		return scale(impacts, factor);
	}

	default double totalImpactOf(int indicator, int techFlow) {
		return totalFactorOf(techFlow) * totalImpactOfOne(indicator, techFlow);
	}

	double[] totalImpacts();

	/**
	 * Contains the direct contributions $\mathbf{k}_s$ of the process-product pairs
	 * to the total net-costs ($\odot$ denotes element-wise multiplication):
	 * <p>
	 * $$\mathbf{k}_s = \mathbf{k} \odot \mathbf{s}$$
	 */
	double directCostsOf(int techFlow);

	double totalCostsOfOne(int techFlow);

	default double totalCostsOf(int techFlow) {
		return totalFactorOf(techFlow) * totalCostsOfOne(techFlow);
	}

	double totalCosts();

	/**
	 * Returns true if the given array is `null` or empty.
	 */
	default boolean isEmpty(double[] values) {
		return values == null || values.length == 0;
	}

	/**
	 * Scales the given vector $\mathbf{v}$ by the given factor $f$ in place:
	 * <p>
	 * $$
	 * \mathbf{v} := \mathbf{v} \odot f
	 * $$
	 */
	default void scaleInPlace(double[] values, double factor) {
		if (isEmpty(values))
			return;
		for (int i = 0; i < values.length; i++) {
			values[i] *= factor;
		}
	}

	/**
	 * Calculates a vector $\mathbf{w}$ by scaling the given vector
	 * $\mathbf{v}$ with the given factor $f$:
	 * <p>
	 * $$
	 * \mathbf{w} = \mathbf{v} \odot f
	 * $$
	 */
	default double[] scale(double[] values, double factor) {
		if (isEmpty(values))
			return EMPTY_VECTOR;
		var w = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			w[i] = values[i] * factor;
		}
		return w;
	}

	default double[] copy(double[] values) {
		if (isEmpty(values))
			return EMPTY_VECTOR;
		return Arrays.copyOf(values, values.length);
	}
}
