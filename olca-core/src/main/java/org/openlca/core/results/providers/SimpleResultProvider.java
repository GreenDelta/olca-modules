package org.openlca.core.results.providers;

import java.util.Arrays;

import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.results.SimpleResult;

/**
 * A SimpleResultProvider just wraps a set of result data. It should be just
 * used with the SimpleResult view as the detailed contributions are not
 * provided.
 */
public class SimpleResultProvider implements ResultProvider {

	private final Demand demand;
	private final TechIndex techIndex;
	private EnviIndex flowIndex;
	private ImpactIndex impactIndex;
	private double[] scalingVector;
	private double[] totalFlows;
	private double[] totalImpacts;
	private double[] totalRequirements;

	private SimpleResultProvider(Demand demand, TechIndex techIndex) {
		this.demand = demand;
		this.techIndex = techIndex;
	}

	public static SimpleResultProvider of(Demand demand, TechIndex techIndex) {
		return new SimpleResultProvider(demand, techIndex);
	}

	public SimpleResultProvider withFlowIndex(EnviIndex flowIndex) {
		this.flowIndex = flowIndex;
		return this;
	}

	public SimpleResultProvider withImpactIndex(ImpactIndex impactIndex) {
		this.impactIndex = impactIndex;
		return this;
	}

	public SimpleResultProvider withScalingVector(double[] s) {
		this.scalingVector = s;
		return this;
	}

	public SimpleResultProvider withTotalRequirements(double[] t) {
		this.totalRequirements = t;
		return this;
	}

	public SimpleResultProvider withTotalFlows(double[] v) {
		this.totalFlows = v;
		return this;
	}

	public SimpleResultProvider withTotalImpacts(double[] v) {
		this.totalImpacts = v;
		return this;
	}

	public SimpleResult toResult() {
		return new SimpleResult(this);
	}

	@Override
	public Demand demand() {
		return demand;
	}

	@Override
	public TechIndex techIndex() {
		return techIndex;
	}

	@Override
	public EnviIndex enviIndex() {
		return flowIndex;
	}

	@Override
	public ImpactIndex impactIndex() {
		return impactIndex;
	}

	@Override
	public boolean hasCosts() {
		return false;
	}

	@Override
	public double[] scalingVector() {
		if (scalingVector != null)
			return scalingVector;
		if (techIndex == null)
			return EMPTY_VECTOR;
		var s = new double[techIndex.size()];
		Arrays.fill(s, 1);
		return s;
	}

	@Override
	public double[] totalRequirements() {
		if (totalRequirements != null)
			return totalRequirements;
		if (techIndex == null)
			return EMPTY_VECTOR;
		var t = new double[techIndex.size()];
		Arrays.fill(t, 1);
		return t;
	}

	@Override
	public double[] techColumnOf(int techFlow) {
		return EMPTY_VECTOR;
	}

	@Override
	public double[] solutionOfOne(int techFlow) {
		return EMPTY_VECTOR;
	}

	@Override
	public double loopFactorOf(int techFlow) {
		return 0;
	}

	@Override
	public double[] unscaledFlowsOf(int techFlow) {
		return EMPTY_VECTOR;
	}

	@Override
	public double[] totalFlowsOfOne(int techFlow) {
		return EMPTY_VECTOR;
	}

	@Override
	public double[] totalFlows() {
		if (totalFlows != null)
			return totalFlows;
		if (flowIndex == null)
			return EMPTY_VECTOR;
		totalFlows = new double[flowIndex.size()];
		return totalFlows;
	}

	@Override
	public double[] impactFactorsOf(int flow) {
		return EMPTY_VECTOR;
	}

	@Override
	public double[] directImpactsOf(int techFlow) {
		return EMPTY_VECTOR;
	}

	@Override
	public double[] totalImpactsOfOne(int techFlow) {
		return EMPTY_VECTOR;
	}

	@Override
	public double[] totalImpacts() {
		if (totalImpacts != null)
			return totalImpacts;
		if (impactIndex == null) {
			return EMPTY_VECTOR;
		}
		totalImpacts = new double[impactIndex.size()];
		return totalImpacts;
	}

	@Override
	public double directCostsOf(int techFlow) {
		return 0;
	}

	@Override
	public double totalCostsOfOne(int techFlow) {
		return 0;
	}

	@Override
	public double totalCosts() {
		return 0;
	}
}
