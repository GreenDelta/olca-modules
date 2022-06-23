package org.openlca.core.results.providers;

import org.openlca.core.matrix.Demand;
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
		return r.directInventories() != null
			? r.directInventories().getColumn(techFlow)
			: EMPTY_VECTOR;
	}

	@Override
	public double directFlowOf(int flow, int techFlow) {
		return r.directInventories() != null
			? r.directInventories().get(flow, techFlow)
			: 0;
	}

	@Override
	public double[] totalFlowsOfOne(int techFlow) {
		return r.inventoryIntensities() != null
			? r.inventoryIntensities().getColumn(techFlow)
			: EMPTY_VECTOR;
	}

	@Override
	public double totalFlowOfOne(int flow, int techFlow) {
		return r.inventoryIntensities() != null
			? r.inventoryIntensities().get(flow, techFlow)
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
	public double totalFlowOf(int flow, int techFlow) {
		var factor = totalFactorOf(techFlow);
		var intensity = totalFlowOfOne(flow, techFlow);
		return factor * intensity;
	}

	@Override
	public double[] totalFlows() {
		return r.totalInventory() != null
			? r.totalInventory()
			: EMPTY_VECTOR;
	}

	@Override
	public double[] impactFactorsOf(int flow) {
		return r.data().impactMatrix != null
			? r.data().impactMatrix.getColumn(flow)
			: EMPTY_VECTOR;
	}

	@Override
	public double impactFactorOf(int indicator, int flow) {
		return r.data().impactMatrix != null
			? r.data().impactMatrix.get(indicator, flow)
			: 0;
	}

	@Override
	public double[] flowImpactsOf(int flow) {
		var totalInterventions = r.totalInventory();
		if (totalInterventions == null)
			return EMPTY_VECTOR;
		var impacts = impactFactorsOf(flow);
		scaleInPlace(impacts, totalInterventions[flow]);
		return impacts;
	}

	@Override
	public double flowImpactOf(int indicator, int flow) {
		var totalInterventions = r.totalInventory();
		if (totalInterventions == null)
			return 0;
		var factor = impactFactorOf(indicator, flow);
		return factor * totalInterventions[flow];
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
