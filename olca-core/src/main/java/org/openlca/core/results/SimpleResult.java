package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.providers.ResultProvider;
import org.openlca.core.results.providers.ResultProviders;

/**
 * The simplest kind of result of a calculated product system. This result type
 * is particularly suitable for Monte Carlo simulations or other quick
 * calculations.
 */
public class SimpleResult extends BaseResult {

	public final ResultProvider provider;

	/**
	 * The scaling vector $\mathbf{s}$ which is calculated by solving the
	 * equation
	 * <p>
	 * $$\mathbf{A} \ \mathbf{s} = \mathbf{f}$$
	 * <p>
	 * where $\mathbf{A}$ is the technology matrix and $\mathbf{f}$ the final
	 * demand vector of the product system.
	 */
	public final double[] scalingVector;

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
	public final double[] totalRequirements;

	/**
	 * The inventory result $\mathbf{g}$ of a product system:
	 * <p>
	 * $$\mathbf{g} = \mathbf{B} \ \mathbf{s}$$
	 * <p>
	 * Where $\mathbf{B}$ is the intervention matrix and $\mathbf{s}$ the
	 * scaling vector. Note that inputs have negative values in this vector.
	 */
	public final double[] totalFlowResults;

	/**
	 * The LCIA result $\mathbf{h}$ of a product system:
	 * <p>
	 * $$\mathbf{h} = \mathbf{C} \ \mathbf{g}$$
	 * <p>
	 * Where $\mathbf{C}$ is a flow * LCIA category matrix with the
	 * characterization factors and $\mathbf{g}$ the inventory result.
	 */
	public final double[] totalImpactResults;

	/**
	 * The total net-costs $k_t$ of the LCC result:
	 * <p>
	 * $$k_t = \mathbf{k} \cdot \mathbf{s}$$
	 * <p>
	 * Where $\mathbf{k}_j$ are the net-costs of process $j$ and $\mathbf{s}_j$
	 * is the scaling factor of that process.
	 */
	public final double totalCosts;

	public SimpleResult(ResultProvider p) {
		this.provider = Objects.requireNonNull(p);
		this.scalingVector = p.scalingVector();
		this.totalRequirements = p.totalRequirements();
		this.totalFlowResults = p.totalFlows();
		this.totalImpactResults = p.totalImpacts();
		this.totalCosts = p.totalCosts();
	}

	public static SimpleResult of(IDatabase db, MatrixData data) {
		var provider = ResultProviders.lazyOf(db, data);
		return new SimpleResult(provider);
	}

	@Override
	public TechIndex techIndex() {
		return provider.techIndex();
	}

	@Override
	public EnviIndex enviIndex() {
		return provider.flowIndex();
	}

	@Override
	public ImpactIndex impactIndex() {
		return provider.impactIndex();
	}

	/**
	 * Get the scaling factor $\mathbf{s}_j$ of the given process-product pair
	 * $j$.
	 */
	public double getScalingFactor(TechFlow product) {
		int idx = techIndex().of(product);
		if (idx < 0 || idx > scalingVector.length)
			return 0;
		return scalingVector[idx];
	}

	/**
	 * Get the scaling factor $\mathbf{s}_j$ of the given process $j$. When the
	 * process has multiple products in the system it returns the sum of the
	 * scaling factors of all of these process-product pairs.
	 */
	public double getScalingFactor(CategorizedDescriptor process) {
		double factor = 0;
		for (TechFlow p : techIndex().getProviders(process)) {
			factor += getScalingFactor(p);
		}
		return factor;
	}

	/**
	 * Get the total inventory result $\mathbf{g}_i$ of the given flow $i$.
	 */
	public double getTotalFlowResult(EnviFlow flow) {
		var flowIndex = enviIndex();
		if (flowIndex == null)
			return 0;
		int idx = flowIndex.of(flow);
		if (idx < 0 || idx >= totalFlowResults.length)
			return 0;
		return adopt(flow, totalFlowResults[idx]);
	}

	/**
	 * Returns the flow results of the inventory result $\mathbf{g}$.
	 */
	public List<FlowResult> getTotalFlowResults() {
		var flowIndex = enviIndex();
		if (flowIndex == null)
			return Collections.emptyList();
		List<FlowResult> results = new ArrayList<>(flowIndex.size());
		flowIndex.each((i, f) -> results.add(
				new FlowResult(f, getTotalFlowResult(f))));
		return results;
	}

	/**
	 * Returns the total LCIA result $\mathbf{h}_i$ of the given LCIA category
	 * $i$.
	 */
	public double getTotalImpactResult(ImpactDescriptor impact) {
		var impactIndex = impactIndex();
		if (impactIndex == null)
			return 0;
		int idx = impactIndex.of(impact);
		if (idx < 0 || idx >= totalImpactResults.length)
			return 0;
		return totalImpactResults[idx];
	}

	/**
	 * Returns the impact category results for the given result.
	 */
	public List<ImpactResult> getTotalImpactResults() {
		var impactIndex = impactIndex();
		if (impactIndex == null)
			return Collections.emptyList();
		var results = new ArrayList<ImpactResult>();
		impactIndex.each((i, d) -> {
			var r = new ImpactResult();
			r.impact = d;
			r.value = getTotalImpactResult(d);
			results.add(r);
		});
		return results;
	}

	@Override
	public boolean hasCosts() {
		return totalCosts != 0;
	}
}
