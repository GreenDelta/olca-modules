package org.openlca.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.core.math.ReferenceAmount;

/**
 * A calculation for a process or product system. In case of a process,
 * we construct the calculation matrices for all processes in the database
 * and link the processes as good as we can for the information in these
 * processes. In case of product systems, only the processes of that
 * system are added to the calculation matrices and are linked as defined
 * in the product system.
 */
public class CalculationSetup extends AbstractEntity
	implements Copyable<CalculationSetup> {

	private CalculationType type;
	private ProductSystem system;
	private Process process;

	private ImpactMethod impactMethod;
	private NwSet nwSet;
	private List<ParameterRedef> parameters;
	private AllocationMethod allocation = AllocationMethod.NONE;
	private boolean withCosts = false;
	private boolean withRegionalization = false;
	private Unit unit;
	private FlowPropertyFactor flowPropertyFactor;
	private Double amount;
	private int numberOfRuns = -1;

	/**
	 * Creates a new calculation setup for the given type and calculation target
	 * (which must be a process or product system). In case of a product system
	 * it does not add the parameter redefinitions of the system to this setup.
	 * Thus, you need to do this in a separate step.
	 */
	public CalculationSetup(CalculationType type, CalculationTarget target) {
		this.type = Objects.requireNonNull(type);
		if (target.isProcess()) {
			this.process = target.asProcess();
			this.system = null;
		} else if (target.isProductSystem()) {
			this.system = target.asProductSystem();
			this.process = null;
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
		return setup;
	}

	public CalculationType type() {
		return type;
	}

	public CalculationSetup withType(CalculationType type) {
		if (type != null) {
			this.type = type;
		}
		return this;
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

	@SuppressWarnings("unchecked")
	public <T extends RootEntity & CalculationTarget> T target() {
		return (T) (system != null
			? system
			: process);
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
	 * Set the calculation target of this setup.
	 *
	 * @param target The process or product system. {@code null} is allowed
	 *               here to support editing features.
	 * @return this calculation setup
	 */
	public CalculationSetup withTarget(CalculationTarget target) {
		if (target == null) {
			process = null;
			system = null;
		} else if (target.isProcess()) {
			process = target.asProcess();
		} else if (target.isProductSystem()) {
			system = target.asProductSystem();
		}
		return this;
	}

	/**
	 * Optionally set the unit for the target amount of the reference flow.
	 * By default, this is the reference unit of that flow.
	 */
	public CalculationSetup withUnit(Unit unit) {
		this.unit = unit;
		return this;
	}

	/**
	 * Get the unit for the target amount of the reference flow.
	 */
	public Unit unit() {
		if (unit != null)
			return unit;
		if (system != null)
			return system.targetUnit;
		if (process == null)
			return null;
		var refFlow = process.quantitativeReference;
		return refFlow == null
			? null
			: refFlow.unit;
	}

	/**
	 * Optionally set the flow property factor for the target amount of
	 * the reference flow. By default this is the reference flow property
	 * factor of that flow.
	 */
	public CalculationSetup withFlowPropertyFactor(FlowPropertyFactor f) {
		this.flowPropertyFactor = f;
		return this;
	}

	/**
	 * Get the flow property factor for the target amount of the reference
	 * flow.
	 */
	public FlowPropertyFactor flowPropertyFactor() {
		if (flowPropertyFactor != null)
			return flowPropertyFactor;
		if (system != null)
			return system.targetFlowPropertyFactor;
		if (process == null)
			return null;
		var refFlow = process.quantitativeReference;
		return refFlow == null
			? null
			: refFlow.flowPropertyFactor;
	}

	/**
	 * Optionally set the target amount for the reference flow of this setup.
	 * By default it is the defined target amount of the product system or
	 * the quantitative reference in case of a process.
	 */
	public CalculationSetup withAmount(double amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * Get the target amount of the reference flow in the unit as defined by
	 * this setup.
	 */
	public double amount() {
		if (amount != null)
			return amount;
		if (system != null)
			return system.targetAmount;
		if (process == null)
			return 1;
		var refFlow = process.quantitativeReference;
		return refFlow == null
			? 1
			: refFlow.amount;
	}

	/**
	 * Get the value for the demand vector for the quantitative reference defined by
	 * this calculation setup. Note that this value is negative for waste treatment
	 * systems and that it is given in the reference unit of the reference flow.
	 */
	public double demand() {
		var value = ReferenceAmount.get(amount(), unit(), flowPropertyFactor());
		var flow = flow();
		return flow != null && flow.flowType == FlowType.WASTE_FLOW
			? -value
			: value;
	}

	/**
	 * Returns the reference flow of this calculation setup, which is the
	 * reference flow of the underlying product system or process of this setup.
	 */
	public Flow flow() {
		var refFlow = system != null
			? system.referenceExchange
			: process != null
			? process.quantitativeReference
			: null;
		return refFlow != null
			? refFlow.flow
			: null;
	}

	public CalculationSetup withImpactMethod(ImpactMethod impactMethod) {
		this.impactMethod = impactMethod;
		return this;
	}

	public ImpactMethod impactMethod() {
		return impactMethod;
	}

	public CalculationSetup withNwSet(NwSet nwSet) {
		this.nwSet = nwSet;
		return this;
	}

	public NwSet nwSet() {
		return nwSet;
	}

	public CalculationSetup withAllocation(AllocationMethod allocation) {
		this.allocation = allocation;
		return this;
	}

	public AllocationMethod allocation() {
		return allocation == null
			? AllocationMethod.NONE
			: allocation;
	}

	/**
	 * Set the parameter redefinitions that should be applied in the calculation.
	 * Note that even if the calculation setup is created for a product system
	 * with parameter redefinitions, no parameter redefinitions are applied by
	 * default. This method will create a copy of the given parameter
	 * redefinitions as these become part of this setup which can be saved in a
	 * database.
	 */
	public CalculationSetup withParameters(List<ParameterRedef> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			this.parameters = null;
			return this;
		}
		this.parameters = new ArrayList<>(parameters.size());
		for (var param : parameters) {
			if (param == null)
				continue;
			this.parameters.add(param.copy());
		}
		return this;
	}

	public List<ParameterRedef> parameters() {
		return parameters == null
			? Collections.emptyList()
			: parameters;
	}

	public CalculationSetup withCosts(boolean b) {
		this.withCosts = b;
		return this;
	}

	public boolean hasCosts() {
		return this.withCosts;
	}

	/**
	 * Set whether a regionalized result should be calculated or not. This
	 * is {@code false} by default. If this is set to true, the intervention
	 * matrix is indexed by (elementary flow, location) - pairs instead of just
	 * elementary flows. The LCI result then contains results for these pairs
	 * which can be then used in regionalized impact assessments.
	 */
	public CalculationSetup withRegionalization(boolean b) {
		this.withRegionalization = b;
		return this;
	}

	public boolean hasRegionalization() {
		return this.withRegionalization;
	}

	/**
	 * This is only valid for Monte Carlo Simulations and returns the number of
	 * simulation runs in this case, otherwise it just returns {@code -1}.
	 */
	public int numberOfRuns() {
		return this.numberOfRuns;
	}

	public CalculationSetup withNumberOfRuns(int n) {
		this.numberOfRuns = n;
		return this;
	}

	@Override
	public CalculationSetup copy() {
		var target = system != null ? system : process;
		var clone = new CalculationSetup(type, target);
		clone.type = type;
		clone.system = system;
		clone.process = process;
		clone.impactMethod = impactMethod;
		clone.nwSet = nwSet;
		clone.allocation = allocation;
		clone.withCosts = withCosts;
		clone.withRegionalization = withRegionalization;
		clone.unit = unit;
		clone.flowPropertyFactor = flowPropertyFactor;
		clone.amount = amount;
		clone.numberOfRuns = numberOfRuns;

		if (parameters != null) {
			clone.parameters = new ArrayList<>(parameters.size());
			for (var param : parameters) {
				clone.parameters.add(param.copy());
			}
		}
		return clone;
	}
}
