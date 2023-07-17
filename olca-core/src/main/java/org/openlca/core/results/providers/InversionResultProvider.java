package org.openlca.core.results.providers;

import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.format.ColumnIterator;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;

/**
 * Implements the result provider interface based on a full inverse of the
 * technology matrix.
 */
public class InversionResultProvider implements ResultProvider {

	private final InversionResult r;

	private InversionResultProvider(InversionResult r) {
		this.r = r;
	}

	public static ResultProvider of(InversionResult r) {
		return new InversionResultProvider(r);
	}

	@Override
	public Demand demand() {
		return r.data().demand;
	}

	@Override
	public TechIndex techIndex() {
		return r.data().techIndex;
	}

	@Override
	public EnviIndex enviIndex() {
		return r.data().enviIndex;
	}

	@Override
	public ImpactIndex impactIndex() {
		return r.data().impactIndex;
	}

	@Override
	public boolean hasCosts() {
		return r.directCosts() != null;
	}

	@Override
	public double[] scalingVector() {
		return r.scalingVector();
	}

	@Override
	public double scalingFactorOf(int techFlow) {
		return scalingVector()[techFlow];
	}

	@Override
	public double[] totalRequirements() {
		return r.totalRequirements();
	}

	@Override
	public double totalRequirementsOf(int techFlow) {
		return totalRequirements()[techFlow];
	}

	@Override
	public double[] techColumnOf(int techFlow) {
		return r.data().techMatrix.getColumn(techFlow);
	}

	@Override
	public ColumnIterator iterateTechColumnOf(int techFlow) {
		return ColumnIterator.of(r.data().techMatrix, techFlow);
	}

	@Override
	public double techValueOf(int row, int col) {
		return r.data().techMatrix.get(row, col);
	}

	@Override
	public double[] solutionOfOne(int techFlow) {
		return r.inverse().getColumn(techFlow);
	}

	@Override
	public double loopFactorOf(int techFlow) {
		return r.loopFactors()[techFlow];
	}

	@Override
	public double[] unscaledFlowsOf(int techFlow) {
		return r.data().enviMatrix != null
			? r.data().enviMatrix.getColumn(techFlow)
			: EMPTY_VECTOR;
	}

	@Override
	public double unscaledFlowOf(int flow, int techFlow) {
		return r.data().enviMatrix != null
			? r.data().enviMatrix.get(flow, techFlow)
			: 0;
	}

	@Override
	public double[] directFlowsOf(int techFlow) {
		return r.directFlows() != null
			? r.directFlows().getColumn(techFlow)
			: EMPTY_VECTOR;
	}

	@Override
	public double directFlowOf(int enviFlow, int techFlow) {
		return r.directFlows() != null
			? r.directFlows().get(enviFlow, techFlow)
			: 0;
	}

	@Override
	public double[] totalFlowsOfOne(int techFlow) {
		return r.flowIntensities() != null
			? r.flowIntensities().getColumn(techFlow)
			: EMPTY_VECTOR;
	}

	@Override
	public double totalFlowOfOne(int enviFlow, int techFlow) {
		return r.flowIntensities() != null
			? r.flowIntensities().get(enviFlow, techFlow)
			: 0;
	}

	@Override
	public double[] totalFlowsOf(int techFlow) {
		var factor = totalFactorOf(techFlow);
		var totals = totalFlowsOfOne(techFlow);
		scaleInPlace(totals, factor);
		return totals;
	}

	@Override
	public double totalFlowOf(int enviFlow, int techFlow) {
		var factor = totalFactorOf(techFlow);
		var intensity = totalFlowOfOne(enviFlow, techFlow);
		return factor * intensity;
	}

	@Override
	public double[] totalFlows() {
		return r.totalFlows() != null
			? r.totalFlows()
			: EMPTY_VECTOR;
	}

	@Override
	public double[] impactFactorsOf(int enviFlow) {
		return r.data().impactMatrix != null
			? r.data().impactMatrix.getColumn(enviFlow)
			: EMPTY_VECTOR;
	}

	@Override
	public double impactFactorOf(int indicator, int enviFlow) {
		return r.data().impactMatrix != null
			? r.data().impactMatrix.get(indicator, enviFlow)
			: 0;
	}

	@Override
	public double[] flowImpactsOf(int enviFlow) {
		var totalInterventions = r.totalFlows();
		if (totalInterventions == null)
			return EMPTY_VECTOR;
		var impacts = impactFactorsOf(enviFlow);
		scaleInPlace(impacts, totalInterventions[enviFlow]);
		return impacts;
	}

	@Override
	public double flowImpactOf(int indicator, int enviFlow) {
		var totalInterventions = r.totalFlows();
		if (totalInterventions == null)
			return 0;
		var factor = impactFactorOf(indicator, enviFlow);
		return factor * totalInterventions[enviFlow];
	}

	@Override
	public double[] directImpactsOf(int techFlow) {
		return r.directImpacts() != null
			? r.directImpacts().getColumn(techFlow)
			: EMPTY_VECTOR;
	}

	@Override
	public double directImpactOf(int indicator, int techFlow) {
		return r.directImpacts() != null
			? r.directImpacts().get(indicator, techFlow)
			: 0;
	}

	@Override
	public double[] totalImpactsOfOne(int techFlow) {
		return r.impactIntensities() != null
			? r.impactIntensities().getColumn(techFlow)
			: EMPTY_VECTOR;
	}

	@Override
	public double totalImpactOfOne(int indicator, int techFlow) {
		return r.impactIntensities() != null
			? r.impactIntensities().get(indicator, techFlow)
			: 0;
	}

	@Override
	public double[] totalImpacts() {
		return r.totalImpacts() != null
			? r.totalImpacts()
			: EMPTY_VECTOR;
	}

	@Override
	public double directCostsOf(int techFlow) {
		return r.directCosts() != null
			? r.directCosts()[techFlow]
			: 0;
	}

	@Override
	public double totalCostsOfOne(int techFlow) {
		return r.costIntensities() != null
			? r.costIntensities()[techFlow]
			: 0;
	}

	@Override
	public double totalCosts() {
		return r.totalCosts();
	}

}
