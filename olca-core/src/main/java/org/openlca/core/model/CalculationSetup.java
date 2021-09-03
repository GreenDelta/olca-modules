package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.core.math.ReferenceAmount;

/**
 * A setup for a product system calculation.
 */
public class CalculationSetup {

	public final CalculationType calculationType;

	public final ProductSystem productSystem;

	public ImpactMethod impactMethod;

	public NwSet nwSet;

	public boolean withCosts = false;


	/**
	 * Indicates whether a regionalized result should be calculated or not. If
	 * this is set to true, the intervention matrix is indexed by (elementary
	 * flow, location) - pairs instead of just elementary flows. The LCI result
	 * then contains results for these pairs which can be then used in
	 * regionalized impact assessments.
	 */
	public boolean withRegionalization = false;

	/**
	 * Indicates whether the calculation matrices should be created with associated
	 * uncertainty distributions. Typically, this is only required for running Monte
	 * Carlo simulations.
	 */
	public boolean withUncertainties = false;

	public AllocationMethod allocationMethod = AllocationMethod.NONE;

	public final List<ParameterRedef> parameterRedefs = new ArrayList<>();

	/**
	 * Only valid for Monte Carlo Simulations (also, withUncertainties needs to be
	 * true in this case).
	 */
	public int numberOfRuns = -1;

	// properties with default values from the product system
	private Unit unit;

	private FlowPropertyFactor flowPropertyFactor;

	private Double amount;

	/**
	 * Creates a new calculation setup for the given type and product system. It
	 * does not add the parameter redefinitions of the product system to this
	 * setup. Thus, you need to do this in a separate step.
	 */
	public CalculationSetup(CalculationType type, ProductSystem system) {
		this.calculationType = Objects.requireNonNull(type);
		this.productSystem = Objects.requireNonNull(system);
	}

	public static CalculationSetup simple(ProductSystem system) {
		return new CalculationSetup(
			CalculationType.SIMPLE_CALCULATION, system);
	}

	public static CalculationSetup contributions(ProductSystem system) {
		return new CalculationSetup(
			CalculationType.CONTRIBUTION_ANALYSIS, system);
	}

	public static CalculationSetup fullAnalysis(ProductSystem system) {
		return new CalculationSetup(
			CalculationType.UPSTREAM_ANALYSIS, system);
	}

	public static CalculationSetup monteCarlo(ProductSystem system, int runs) {
		var setup = new CalculationSetup(
			CalculationType.MONTE_CARLO_SIMULATION, system);
		setup.numberOfRuns = runs;
		setup.withUncertainties = true;
		return setup;
	}

	/**
	 * Optionally set another unit for the calculation than the one defined in
	 * the product system.
	 */
	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	/**
	 * Get the unit of the quantitative reference of the product system. By
	 * default this is the reference unit of the underlying product system.
	 */
	public Unit getUnit() {
		if (unit != null)
			return unit;
		else
			return productSystem.targetUnit;
	}

	/**
	 * Optionally set another flow property factor for the calculation than the
	 * one defined in the product system.
	 */
	public void setFlowPropertyFactor(FlowPropertyFactor flowPropertyFactor) {
		this.flowPropertyFactor = flowPropertyFactor;
	}

	public FlowPropertyFactor getFlowPropertyFactor() {
		if (flowPropertyFactor != null)
			return flowPropertyFactor;
		else
			return productSystem.targetFlowPropertyFactor;
	}

	/**
	 * Optionally set another target amount for the calculation than the one
	 * defined in the product system.
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}

	/**
	 * Get the target amount in the unit of this calculation setup.
	 */
	public double getAmount() {
		return amount != null
			? amount
			: productSystem.targetAmount;
	}

	/**
	 * Get the value for the demand vector for the quantitative reference defined by
	 * this calculation setup. Note that this value is negative for waste treatment
	 * systems and that it is given in the reference unit of the reference flow.
	 */
	public double getDemandValue() {
		double a = amount != null
			? amount
			: productSystem.targetAmount;
		a = ReferenceAmount.get(a, getUnit(), getFlowPropertyFactor());
		if (productSystem.referenceExchange == null)
			return a;
		Flow flow = productSystem.referenceExchange.flow;
		if (flow != null && flow.flowType == FlowType.WASTE_FLOW) {
			return -a;
		}
		return a;
	}
}
