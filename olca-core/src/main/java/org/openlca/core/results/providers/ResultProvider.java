package org.openlca.core.results.providers;

import java.util.Arrays;

import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.MatrixIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.descriptors.ImpactDescriptor;

/**
 * The general interface of a result provider. The documentation is based on
 * matrix algebra but this does not mean that an implementation has to be based
 * on matrices; it just has to implement this protocol. The returned values of
 * the methods should be treated as live views on the underlying data and thus,
 * should be **never** modified. The default implementations often have
 * copy-behaviour, and it is often much more efficient to overwrite them in a
 * specific implementation. All returned results are given in the reference
 * units of the respective result elements (e.g. for flows; for costs it is the
 * reference currency). For flow results, input amounts have a negative sign.
 * Also, characterization factors of impact categories where the impact
 * direction is `input` have a negative sign.
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
	 * Returns the matrix index of the given technosphere flow. Returns {@code -1}
	 * if it is not part of the technosphere index of the system.
	 */
	default int indexOf(TechFlow techFlow) {
		return techIndex().of(techFlow);
	}

	/**
	 * The index of the rows of the intervention matrix `B` and the columns of the
	 * characterization matrix `C`. It contains the flows that cross the boundary
	 * of the system with the environment in the respective matrix order. In LCA,
	 * these flows are called 'elementary flows' and openLCA has a flow type for
	 * these kind of flows. However, this index can contain other flow types that
	 * cross the boundary with the environment (e.g. unlinked product and waste
	 * flows). Still, we use the term 'elementary flow' in the documentation for
	 * these flows.
	 */
	EnviIndex enviIndex();

	/**
	 * Returns the matrix index of the given elementary flow. Returns {@code -1}
	 * if it is not contained in the elementary flow index of the system.
	 */
	default int indexOf(EnviFlow enviFlow) {
		var idx = enviIndex();
		return idx != null
				? idx.of(enviFlow)
				: -1;
	}

	/**
	 * The row index of the matrix with the characterization factors `C` (the
	 * impact matrix). It contains the impact categories in the respective matrix
	 * order.
	 */
	ImpactIndex impactIndex();

	/**
	 * Returns the matrix index of the given impact category. Returns {@code -1}
	 * if it is not contained in the impact category index of the system.
	 */
	default int indexOf(ImpactDescriptor indicator) {
		var idx = impactIndex();
		return idx != null
				? idx.of(indicator)
				: -1;
	}

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
	 * The total requirements of technosphere flows to fulfill the demand of the
	 * product system. As our technology matrix `A` is indexed symmetrically
	 * (means rows and columns refer to the same technosphere flows) our product
	 * amounts are on the diagonal of the technology matrix `A` and the total
	 * requirements can be calculated by the following equation where `s` is the
	 * scaling vector: `t = diag(s) * diag(A)`.
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
	 * Get the loop factor for the given technosphere flow. The loop factor `lf[j]`
	 * describes the faction of the total requirements of a technosphere flow `j`
	 * that is not related to loops. It is calculated in the following way:
	 * `lf[j] = 1 / (A[j,j] * INV[j,j])`. A loop factor of 1 means that `j` is not
	 * in a loop; otherwise a small loop factor `0 &lt; lf &lt; 1` means a high
	 * contribution of loops. Loop factors are used to avoid double counting of
	 * loops in some result views.
	 *
	 * @see #totalFactorOf(int)
	 */
	double loopFactorOf(int techFlow);

	/**
	 * The total factor `tf[j]` of a technosphere flow `j` are the total
	 * requirements `t[j]` of `j` multiplied by the loop factor `lf[j]`:
	 * `tf[j] = t[j] * lf[j]`. These are then the total requirements without the
	 * fraction related to loops. This can be then used to calculate total
	 * results related to any flow `j` in the supply chain.
	 * <p>
	 * For example, `M` is the intensity matrix calculated by `M = B * INV`. It
	 * contains in the column `j` the total inventory result related to 1 unit of
	 * the technosphere flow `j`. Now we want to scale it to the total
	 * requirements of `j` that is used in the product system. Multiplying the
	 * total requirements with `M[:,j]` would double count the loop contributions
	 * because they are already contained in `M`.
	 *
	 * <pre>
	 *   {@code
	 * # example can be executed in Julia
	 * using LinearAlgebra
	 *
	 * # the technology matrix
	 * A = [ 0.9 -0.1 ;
	 *      -0.6  0.8 ]
	 *
	 * # the intervention matrix
	 * B = [ 0.0  3.0 ]
	 *
	 * # the final demand vector
	 * f = [ 1.0 ; 0.0 ]
	 *
	 * # calculate the inverse and the scaling vector
	 * INV = A^-1
	 * s = INV[:,1]
	 *
	 * # the total inventory result
	 * g = B * s  # 2.72727
	 *
	 * # the intensity matrix
	 * M = B * INV  # [ 2.72727  4.09091]
	 *
	 * # total requirements
	 * t = diag(A) .* s
	 *
	 * # results with double counting the loop
	 * g1 = M[:,1] .* t[1]  # 2.9752
	 * g2 = M[:,2] .* t[2]  # 2.9752
	 *
	 * # loop factor correction
	 * lf = [1/(A[1,1]*INV[1,1]) ; 1/(A[2,2]*INV[2,2])]
	 * g1 = M[:,1] .* t[1] .* lf[1]  # 2.72727
	 * g2 = M[:,2] .* t[2] .* lf[2]  # 2.72727
	 * }
	 * </pre>
	 *
	 * @see #totalRequirementsOf(int)
	 * @see #loopFactorOf(int)
	 * @see #totalFlowOf(int, int)
	 * @see #totalImpactOf(int, int)
	 * @see #totalCostsOf(int)
	 */
	default double totalFactorOf(int techFlow) {
		var t = totalRequirementsOf(techFlow);
		var loop = loopFactorOf(techFlow);
		return loop * t;
	}

	/**
	 * Get the unscaled column `B[:,j]` for the technosphere flow `j` from the
	 * intervention matrix `B`.
	 */
	double[] unscaledFlowsOf(int techFlow);

	/**
	 * Get the unscaled value `B[i,j]` of the elementary flow `i` for the
	 * technosphere flow `j` from the intervention matrix `B`.
	 */
	default double unscaledFlowOf(int flow, int product) {
		double[] column = unscaledFlowsOf(product);
		return column[flow];
	}

	/**
	 * Returns the direct flow results for the given technosphere flow `j`. This
	 * is the scaled column `j` of the intervention matrix `B`: `s[j] * B[:,j]`.
	 * The sum of all direct flow results is the total inventory result of the
	 * system.
	 */
	default double[] directFlowsOf(int techFlow) {
		var flows = unscaledFlowsOf(techFlow);
		var s = scalingVector();
		if (isEmpty(flows) || isEmpty(s))
			return EMPTY_VECTOR;
		return scale(flows, s[techFlow]);
	}

	/**
	 * Get the direct result of the elementary flow `i` related to the total
	 * requirements of the technosphere flow `j` in the system: `s[j] * B[i,j]`.
	 *
	 * @see #directFlowsOf(int)
	 */
	default double directFlowOf(int enviFlow, int techFlow) {
		return scalingFactorOf(techFlow) * unscaledFlowOf(enviFlow, techFlow);
	}

	/**
	 * Returns the total flow results (direct + upstream) related to 1 unit of
	 * the technosphere flow `j` in the system. This is the respective
	 * column `M[:,j]` of the intensity matrix `M`, where `M = B * A^-1`.
	 */
	double[] totalFlowsOfOne(int techFlow);

	/**
	 * Returns the total result (direct + upstream) of the given elementary
	 * flow `i` related to 1 unit of the technosphere flow `j` in the system. It
	 * is the entry `M[i,j]` of the intensity matrix `M`.
	 *
	 * @see #totalFlowsOfOne(int)
	 */
	default double totalFlowOfOne(int enviFlow, int techFlow) {
		var totals = totalFlowsOfOne(techFlow);
		return isEmpty(totals)
				? 0
				: totals[enviFlow];
	}

	/**
	 * Returns the total flow results (direct + upstream) related to the total
	 * requirements of the technosphere flow `j` in the system. This is the
	 * respective column `M[:,j]` of the intensity matrix `M` scaled by the
	 * total factor `tf`: `M[:,j] * tf[j]`.
	 *
	 * @see #totalFactorOf(int)
	 */
	default double[] totalFlowsOf(int techFlow) {
		var factor = totalFactorOf(techFlow);
		var totals = totalFlowsOfOne(techFlow);
		return scale(totals, factor);
	}

	/**
	 * Returns the total result (direct + upstream) of the given elementary
	 * flow `i` related to the total requirements of the technosphere flow `j`
	 * in the system. This is the entry `M[i,j]` of the intensity matrix `M`
	 * scaled by the total factor `tf`: `M[i,j] * tf[j]`.
	 *
	 * @see #totalFlowsOf(int)
	 */
	default double totalFlowOf(int enviFlow, int techFlow) {
		return totalFactorOf(techFlow) * totalFlowOfOne(enviFlow, techFlow);
	}

	/**
	 * Returns the total elementary flow result `g` (inventory result, LCI result)
	 * of the product system: `g = B * s`, where `B` is the intervention matrix
	 * and `s` the scaling vector.
	 */
	double[] totalFlows();

	/**
	 * Returns the impact factors (characterisation factors) for the elementary
	 * flow `i`. This is the column `C[:,i]` of the impact matrix `C`.
	 */
	double[] impactFactorsOf(int enviFlow);

	/**
	 * Returns the impact factor (characterisation factor) for the impact category
	 * `k` and elementary flow `i`. This is the entry `C[k,i] of the impact
	 * matrix `C`.
	 */
	default double impactFactorOf(int indicator, int enviFlow) {
		var factors = impactFactorsOf(enviFlow);
		return isEmpty(factors)
				? 0
				: factors[indicator];
	}

	/**
	 * Returns the impact results related to the total flow result of the
	 * elementary flow `i`. This is the characterisation factors `C[:,i]`
	 * multiplied with the total inventory result `g[i]`: `C[:,i] * g[i]`.
	 */
	default double[] flowImpactsOf(int enviFlow) {
		var totals = totalFlows();
		var factors = impactFactorsOf(enviFlow);
		if (isEmpty(totals) || isEmpty(factors))
			return EMPTY_VECTOR;
		return scale(factors, totals[enviFlow]);
	}

	/**
	 * Returns the impact result of the impact category `k` related to the total
	 * flow result of the elementary flow `i`. This is the characterisation
	 * factor `C[k:,i]` multiplied with the total inventory result `g[i]`:
	 * `C[k,i] * g[i]`.
	 */
	default double flowImpactOf(int indicator, int enviFlow) {
		var totals = totalFlows();
		var factor = impactFactorOf(indicator, enviFlow);
		return isEmpty(totals)
				? 0
				: factor * totals[enviFlow];
	}

	/**
	 * Returns the direct impact results for the given technosphere flow `j`.
	 * This is the column `H[:,j]` of the direct impact matrix `H`. `H` is
	 * calculated by multiplying the impact matrix `C`, which contains the
	 * characterization factors, with the scaled intervention matrix `G`,
	 * with `G = B * diag(s)`.
	 */
	double[] directImpactsOf(int techFlow);

	/**
	 * Returns the direct impact result of the impact category `k` for the
	 * technosphere flow `j`. This is the entry `H[k,j]` of the direct impact
	 * matrix `H`.
	 *
	 * @see #directImpactsOf(int)
	 */
	default double directImpactOf(int indicator, int techFlow) {
		var impacts = directImpactsOf(techFlow);
		return isEmpty(impacts)
				? 0
				: impacts[indicator];
	}

	/**
	 * Returns the total impact result (direct + upstream) related to 1 unit of
	 * the technosphere flow `j` in the system. This is the respective column
	 * `N[:,j]` of the impact intensity matrix `N` which is calculated by
	 * multiplying the impact matrix `C`, which contains the characterisation
	 * factors, with the inventory intensity matrix `M`: `N = C * M`. `M` is
	 * calculated by multiplying the intervention matrix `B` with the inverse of
	 * the technology matrix `A`: `M = B * A^-1`.
	 *
	 * @see #totalFlowsOfOne(int)
	 */
	double[] totalImpactsOfOne(int techFlow);

	/**
	 * Returns the total result (direct + upstream) of the impact category `k`
	 * related to 1 unit of the technosphere flow `j` in the system. This is the
	 * entry `N[k,j]` of the impact intensity matrix `N`.
	 *
	 * @see #totalImpactsOfOne(int)
	 */
	default double totalImpactOfOne(int indicator, int techFlow) {
		var impacts = totalImpactsOfOne(techFlow);
		return isEmpty(impacts)
				? 0
				: impacts[indicator];
	}

	/**
	 * Returns the total impact assessment results (direct + upstream) related to
	 * the total requirements of the technosphere flow `j` in the system. This is
	 * the respective column `N[:,j]` of the impact intensity matrix `N` scaled by
	 * the total factor `tf[j]`: `N[:,j] * tf[j]`, where `N = C * M` is the impact
	 * intensity matrix and `M = B * INV` the inventory intensity matrix.
	 *
	 * @see #totalFactorOf(int)
	 */
	default double[] totalImpactsOf(int techFlow) {
		var impacts = totalImpactsOfOne(techFlow);
		var factor = totalFactorOf(techFlow);
		return scale(impacts, factor);
	}

	/**
	 * Returns the total result (direct + upstream) of the given impact category
	 * `k` related to the total requirements of the technosphere flow `j` in the
	 * system. This is the entry `N[k,j]` of the impact intensity matrix `N`
	 * scaled by the total factor `tf`: `N[k,j] * tf[j]`
	 *
	 * @see #totalFactorOf(int)
	 * @see #totalImpactsOf(int)
	 */
	default double totalImpactOf(int indicator, int techFlow) {
		return totalFactorOf(techFlow) * totalImpactOfOne(indicator, techFlow);
	}

	/**
	 * Returns the total impact assessment result `h` (the LCIA result) of the
	 * product system: `h = C * g`, where `C` is the impact matrix, which contains
	 * the characterisation factors, and `g` is the inventory result of the system.
	 */
	double[] totalImpacts();

	/**
	 * Returns the direct cost result for the given technosphere flow `j`. This is
	 * the entry `k[j]` of the cost vector scaled by the scaling factor `s[j]`:
	 * `k[j] * s[j]`.
	 */
	double directCostsOf(int techFlow);

	/**
	 * Returns the total cost result (direct + upstream) related to 1 unit of the
	 * technosphere flow `j` in the system. This is the entry `o[j]` of the cost
	 * intensity vector `o = k * INV`, where `k` is the cost vector of the system.
	 */
	double totalCostsOfOne(int techFlow);

	/**
	 * Returns the total cost result (direct + upstream) related to the total
	 * requirements of the technosphere flow `j` in the system. This is the entry
	 * `o[j]` of the cost intensity vector scaled by the total factor `tf[j]`:
	 * `o[j] * tf[j]` where `o = k * INV` are the cost intensities.
	 *
	 * @see #totalFactorOf(int)
	 */
	default double totalCostsOf(int techFlow) {
		return totalFactorOf(techFlow) * totalCostsOfOne(techFlow);
	}

	/**
	 * Returns the total cost result (the LCC result) of the product system. This
	 * is calculated by multiplying the cost vector `k`, which is a row vector,
	 * with the scaling vector `s`: `k * s`.
	 */
	double totalCosts();

	/**
	 * Returns {@code true} if the given array is {@code null} or empty (has a
	 * length of 0).
	 */
	default boolean isEmpty(double[] values) {
		return values == null || values.length == 0;
	}

	/**
	 * Scales the given vector `v` by the given factor `f` in place: `v := v * f`.
	 */
	default void scaleInPlace(double[] values, double factor) {
		if (isEmpty(values))
			return;
		for (int i = 0; i < values.length; i++) {
			values[i] *= factor;
		}
	}

	/**
	 * Creates a new vector `w` by scaling (multiplying) the values of the given
	 * vector `v` with the given factor `f`: `w = v * f`.
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

	/**
	 * Creates a copy of the given vector.
	 */
	default double[] copy(double[] values) {
		return isEmpty(values)
				? EMPTY_VECTOR
				: Arrays.copyOf(values, values.length);
	}

	/**
	 * Switches the sign for input-flows otherwise the value is returned
	 * unchanged. Internally, values and characterization factors of input-flows
	 * are stored as negative values in the underlying matrices of a result. For
	 * user views of these results, we need to change the sign of these flows.
	 */
	static double flowValueView(EnviFlow flow, double value) {
		if (flow == null || !flow.isInput())
			return value;
		// avoid -0 in the results; note that abs(value) is not correct because the
		// original value could be negative ~> -(-(-v))
		return value == 0 ? 0 : -value;
	}
}
