package org.openlca.core.results.providers;

import java.util.Arrays;

import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.MatrixIndex;
import org.openlca.core.matrix.index.TechIndex;

/**
 * The general interface of a result provider. The documentation is based on
 * matrix algebra but this does not mean that an implementation has to be based
 * on matrices; it just has to implement this protocol. The returned values of
 * the methods should be treated as live views on the underlying data and thus,
 * should be **never** modified. The default implementations often have
 * copy-behaviour, and it is often much more efficient to overwrite them in a
 * specific implementation. All returned results are given in the reference
 * units of the respective result elements (e.g. for flows; for costs it is the
 * reference currency).
 */
public interface ResultProvider {

	double[] EMPTY_VECTOR = new double[0];

	/**
	 * The demand to which the result is related.
	 */
	Demand demand();

	/**
	 * The index of the technology matrix `A`. It maps the technosphere flows
	 * symmetrically to the rows and columns of the `A` matrix. A technosphere
	 * flow is a pair of a provider and a flow, where the provider can be a
	 * process, product system, or result.
	 */
	TechIndex techIndex();

	/**
	 * The index of the rows of the intervention matrix `B` and the columns of the
	 * characterization matrix `C`. It contains the flows that cross the boundary
	 * of the system with the environment in the respective matrix order.
	 */
	EnviIndex enviIndex();

	/**
	 * The row index of the matrix with the characterization factors `C` (the
	 * impact matrix). It contains the impact categories in the respective matrix
	 * order.
	 */
	ImpactIndex impactIndex();

	/**
	 * Returns {@code true} when the result contains results for flows (has an
	 * inventory result).
	 */
	default boolean hasFlows() {
		return MatrixIndex.isPresent(enviIndex());
	}

	/**
	 * Returns {@code true} when the result contains results for impact categories
	 * (has an impact assessment result).
	 */
	default boolean hasImpacts() {
		return MatrixIndex.isPresent(impactIndex());
	}

	/**
	 * Returns {@code true} when the result contains results for costs.
	 */
	boolean hasCosts();

	/**
	 * The scaling vector `s` which is calculated by solving the equation
	 * `A * s = f`, where `A` is the technology matrix and `f` the final
	 * demand vector of the product system.
	 */
	double[] scalingVector();

	/**
	 * Returns the scaling factor `s[j]` of the technosphere flow `j`.
	 *
	 * @see #scalingVector()
	 */
	default double scalingFactorOf(int techFlow) {
		var s = scalingVector();
		return isEmpty(s)
			? 0
			: s[techFlow];
	}

	/**
	 * The total requirements of the technosphere flow to fulfill the demand of
	 * the product system. As our technology matrix `A` is indexed symmetrically
	 * (means rows and columns refer to the same technosphere flows) our product
	 * amounts are on the diagonal of the `A` and the total requirements can be
	 * calculated by the following equation where `s` is the scaling vector
	 *  (`.*` denotes element-wise multiplication): `t = diag(A) .* s`
	 */
	default double[] totalRequirements() {
		var index = techIndex();
		var t = new double[index.size()];
		for (int i = 0; i < t.length; i++) {
			t[i] = scaledTechValueOf(i, i);
		}
		return t;
	}

	/**
	 * Returns the total requirements `t[j]` of the technosphere flow `j`.
	 *
	 * @see #totalRequirements()
	 */
	default double totalRequirementsOf(int techFlow) {
		var t = totalRequirements();
		return isEmpty(t)
			? 0
			: t[techFlow];
	}

	/**
	 * Get the unscaled column `A[:,j]` of the technosphere flow `j` from the
	 * technology matrix `A`.
	 *
	 * @see #techValueOf(int, int)
	 */
	double[] techColumnOf(int techFlow);

	/**
	 * Get the unscaled value `A[i,j]` from the technology matrix `A` for the
	 * technosphere flows `i` and `j`. For non-zero values, `j` is a process
	 * which is linked to the provider `i` by the amount `A[i,j]`.
	 */
	default double techValueOf(int i, int j) {
		double[] column = techColumnOf(j);
		return column[i];
	}

	/**
	 * Get the scaled value `s[j] * A[i,j]` of the technology matrix `A`. On
	 * the diagonal of `A`, these are the total requirements of the system,
	 * otherwise a non-zero value is the scaled direct requirement of the provided
	 * flow `i` in the process `j` (summing up all the scaled and negated direct
	 * requirements and possible demand values of `i` gives the total requirements
	 * of `i`).
	 */
	default double scaledTechValueOf(int i, int j) {
		return scalingFactorOf(j) * techValueOf(i, j);
	}

	/**
	 * Get the scaling vector related to the demand of 1 unit of the given
	 * technosphere flow `j`. This is equivalent to the column `j` of the
	 * inverse technology matrix `INV[j]`.
	 */
	double[] solutionOfOne(int techFlow);

	/**
	 * Get the loop factor for the given technosphere flow. The loop factor `lf`
	 * describes the faction of the total requirements of a technosphere flow `j`
	 * that is not related to loops. It is calculated in the following way:
	 * `lf = 1 / (A[j,j] * INV[j,j]). A loop factor of 1 means that `j` is not
	 * in a loop; otherwise a small loop factor `0 < lf < 1` means a high
	 * contribution of loops. Loop factors are used to avoid double counting of
	 * loops in some result views.
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
