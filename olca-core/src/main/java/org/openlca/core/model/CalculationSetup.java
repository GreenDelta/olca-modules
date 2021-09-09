package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.core.math.ReferenceAmount;

/**
 * A calculation for a process or product system. In case of a process,
 * we construct the calculation matrices for all processes in the database
 * and link the processes as best as we can for the information in these
 * processes. In case of product systems, only the processes of that
 * system are added to the calculation matrices and are linked as defined
 * in the product system.
 */
public class CalculationSetup {

	private ProductSystem system;
	private Process process;
	private CalculationType type;
	private ImpactMethod impactMethod;
	private NwSet nwSet;
	private boolean withCosts = false;
	private boolean withRegionalization = false;
	private boolean withUncertainties = false;
	private AllocationMethod allocationMethod = AllocationMethod.NONE;
	private final List<ParameterRedef> parameterRedefs = new ArrayList<>();
	private Unit unit;
	private FlowPropertyFactor flowPropertyFactor;
	private Double amount;

	/**
	 * Indicates whether a regionalized result should be calculated or not. If
	 * this is set to true, the intervention matrix is indexed by (elementary
	 * flow, location) - pairs instead of just elementary flows. The LCI result
	 * then contains results for these pairs which can be then used in
	 * regionalized impact assessments.
	 */


	/**
	 * Indicates whether the calculation matrices should be created with associated
	 * uncertainty distributions. Typically, this is only required for running Monte
	 * Carlo simulations.
	 */






	/**
	 * Only valid for Monte Carlo Simulations (also, withUncertainties needs to be
	 * true in this case).
	 */
	private int numberOfRuns = -1;

	

	/**
	 * The default constructor which is required for our persistence framework.
	 * You should use one of the factory methods in application code instead.
	 */
	public CalculationSetup() {
		type = CalculationType.CONTRIBUTION_ANALYSIS;
	}

	/**
	 * Creates a new calculation setup for the given type and calculation target
	 * (which must be a process or product system). In case of a product system
	 * it does not add the parameter redefinitions of the system to this setup.
	 * Thus, you need to do this in a separate step.
	 */
	public CalculationSetup(CalculationType type, CalculationTarget target) {
		this.type = Objects.requireNonNull(type);
		if (target instanceof Process) {
			this.process = (Process) target;
		} else if (target instanceof ProductSystem) {
			this.system = (ProductSystem) target;
		} else {
			throw new IllegalArgumentException(
				"Unexpected calculation target: " + target
					+ "; only processes and product systems are supported");
		}
	}

	public static CalculationSetup simple(CalculationTarget system) {
		return new CalculationSetup(
			CalculationType.SIMPLE_CALCULATION, system);
	}

	public static CalculationSetup contributions(CalculationTarget system) {
		return new CalculationSetup(
			CalculationType.CONTRIBUTION_ANALYSIS, system);
	}

	public static CalculationSetup fullAnalysis(CalculationTarget system) {
		return new CalculationSetup(
			CalculationType.UPSTREAM_ANALYSIS, system);
	}

	public static CalculationSetup monteCarlo(CalculationTarget system, int runs) {
		var setup = new CalculationSetup(
			CalculationType.MONTE_CARLO_SIMULATION, system);
		setup.numberOfRuns = runs;
		setup.withUncertainties = true;
		return setup;
	}

	/**
	 * Returns true if this setup has a process as calculation target.
	 */
	public boolean hasProcess() {
		return process != null;
	}

	/**
	 * Get the reference process of this calculation setup. In case of a product
	 * system as calculation target, it returns the reference process of that
	 * product system.
	 */
	public Process process() {
		if (process != null)
			return process;
		return system != null
			? system.referenceProcess
			: null;
	}

	/**
	 * Returns true if this setup has a product system as calculation target.
	 */
	public boolean hasProductSystem() {
		return system != null;
	}

	/**
	 * Returns the product system of this setup.
	 */
	public ProductSystem productSystem() {
		return system;
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
			return system.targetUnit;
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
			return system.targetFlowPropertyFactor;
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
			: system.targetAmount;
	}

	/**
	 * Get the value for the demand vector for the quantitative reference defined by
	 * this calculation setup. Note that this value is negative for waste treatment
	 * systems and that it is given in the reference unit of the reference flow.
	 */
	public double getDemandValue() {
		double a = amount != null
			? amount
			: system.targetAmount;
		a = ReferenceAmount.get(a, getUnit(), getFlowPropertyFactor());
		if (system.referenceExchange == null)
			return a;
		Flow flow = system.referenceExchange.flow;
		if (flow != null && flow.flowType == FlowType.WASTE_FLOW) {
			return -a;
		}
		return a;
	}
}
