package org.openlca.core.results.providers;

import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
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
		return null;
	}

	@Override
	public TechIndex techIndex() {
		return null;
	}

	@Override
	public EnviIndex flowIndex() {
		return null;
	}

	@Override
	public ImpactIndex impactIndex() {
		return null;
	}

	@Override
	public boolean hasFlows() {
		return ResultProvider.super.hasFlows();
	}

	@Override
	public boolean hasImpacts() {
		return ResultProvider.super.hasImpacts();
	}

	@Override
	public boolean hasCosts() {
		return false;
	}

	@Override
	public double[] scalingVector() {
		return new double[0];
	}

	@Override
	public double scalingFactorOf(int product) {
		return ResultProvider.super.scalingFactorOf(product);
	}

	@Override
	public double[] totalRequirements() {
		return ResultProvider.super.totalRequirements();
	}

	@Override
	public double totalRequirementsOf(int product) {
		return ResultProvider.super.totalRequirementsOf(product);
	}

	@Override
	public double[] techColumnOf(int product) {
		return new double[0];
	}

	@Override
	public double techValueOf(int row, int col) {
		return ResultProvider.super.techValueOf(row, col);
	}

	@Override
	public double scaledTechValueOf(int row, int col) {
		return ResultProvider.super.scaledTechValueOf(row, col);
	}

	@Override
	public double[] solutionOfOne(int product) {
		return new double[0];
	}

	@Override
	public double loopFactorOf(int product) {
		return 0;
	}

	@Override
	public double totalFactorOf(int product) {
		return ResultProvider.super.totalFactorOf(product);
	}

	@Override
	public double[] unscaledFlowsOf(int product) {
		return new double[0];
	}

	@Override
	public double unscaledFlowOf(int flow, int product) {
		return ResultProvider.super.unscaledFlowOf(flow, product);
	}

	@Override
	public double[] directFlowsOf(int product) {
		return ResultProvider.super.directFlowsOf(product);
	}

	@Override
	public double directFlowOf(int flow, int product) {
		return ResultProvider.super.directFlowOf(flow, product);
	}

	@Override
	public double[] totalFlowsOfOne(int product) {
		return new double[0];
	}

	@Override
	public double totalFlowOfOne(int flow, int product) {
		return ResultProvider.super.totalFlowOfOne(flow, product);
	}

	@Override
	public double[] totalFlowsOf(int product) {
		return ResultProvider.super.totalFlowsOf(product);
	}

	@Override
	public double totalFlowOf(int flow, int product) {
		return ResultProvider.super.totalFlowOf(flow, product);
	}

	@Override
	public double[] totalFlows() {
		return new double[0];
	}

	@Override
	public double[] impactFactorsOf(int flow) {
		return new double[0];
	}

	@Override
	public double impactFactorOf(int indicator, int flow) {
		return ResultProvider.super.impactFactorOf(indicator, flow);
	}

	@Override
	public double[] flowImpactsOf(int flow) {
		return ResultProvider.super.flowImpactsOf(flow);
	}

	@Override
	public double flowImpactOf(int indicator, int flow) {
		return ResultProvider.super.flowImpactOf(indicator, flow);
	}

	@Override
	public double[] directImpactsOf(int product) {
		return new double[0];
	}

	@Override
	public double directImpactOf(int indicator, int product) {
		return ResultProvider.super.directImpactOf(indicator, product);
	}

	@Override
	public double[] totalImpactsOfOne(int product) {
		return new double[0];
	}

	@Override
	public double totalImpactOfOne(int indicator, int product) {
		return ResultProvider.super.totalImpactOfOne(indicator, product);
	}

	@Override
	public double[] totalImpactsOf(int product) {
		return ResultProvider.super.totalImpactsOf(product);
	}

	@Override
	public double totalImpactOf(int indicator, int product) {
		return ResultProvider.super.totalImpactOf(indicator, product);
	}

	@Override
	public double[] totalImpacts() {
		return new double[0];
	}

	@Override
	public double directCostsOf(int product) {
		return 0;
	}

	@Override
	public double totalCostsOfOne(int product) {
		return 0;
	}

	@Override
	public double totalCostsOf(int product) {
		return ResultProvider.super.totalCostsOf(product);
	}

	@Override
	public double totalCosts() {
		return 0;
	}

	@Override
	public boolean isEmpty(double[] values) {
		return ResultProvider.super.isEmpty(values);
	}

	@Override
	public void scaleInPlace(double[] values, double factor) {
		ResultProvider.super.scaleInPlace(values, factor);
	}

	@Override
	public double[] scale(double[] values, double factor) {
		return ResultProvider.super.scale(values, factor);
	}

	@Override
	public double[] copy(double[] values) {
		return ResultProvider.super.copy(values);
	}
}
