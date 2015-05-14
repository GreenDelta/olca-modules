package org.openlca.core.math;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;

/**
 * A setup for a product system calculation.
 */
public class CalculationSetup {

	private final ProductSystem productSystem;
	private Unit unit;
	private FlowPropertyFactor flowPropertyFactor;
	private Double amount;
	private ImpactMethodDescriptor impactMethod;
	private NwSetDescriptor nwSet;
	private AllocationMethod allocationMethod;
	private int numberOfRuns = -1;
	private List<ParameterRedef> parameterRedefs = new ArrayList<>();

	public CalculationSetup(ProductSystem productSystem) {
		this.productSystem = productSystem;
	}

	public ProductSystem getProductSystem() {
		return productSystem;
	}

	public void setImpactMethod(ImpactMethodDescriptor impactMethod) {
		this.impactMethod = impactMethod;
	}

	public ImpactMethodDescriptor getImpactMethod() {
		return impactMethod;
	}

	public void setNwSet(NwSetDescriptor nwSet) {
		this.nwSet = nwSet;
	}

	public NwSetDescriptor getNwSet() {
		return nwSet;
	}

	public void setAllocationMethod(AllocationMethod allocationMethod) {
		this.allocationMethod = allocationMethod;
	}

	public AllocationMethod getAllocationMethod() {
		if (allocationMethod == null)
			return AllocationMethod.NONE;
		return allocationMethod;
	}

	/**
	 * Only valid for sensitivity analysis of Monte-Carlo-Simulations.
	 */
	public void setNumberOfRuns(int numberOfRuns) {
		this.numberOfRuns = numberOfRuns;
	}

	/**
	 * Only valid for sensitivity analysis of Monte-Carlo-Simulations.
	 */
	public int getNumberOfRuns() {
		return numberOfRuns;
	}

	public List<ParameterRedef> getParameterRedefs() {
		return parameterRedefs;
	}

	/**
	 * Optionally set another unit for the calculation than the one defined in
	 * the product system.
	 */
	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Unit getUnit() {
		if (unit != null)
			return unit;
		else
			return productSystem.getTargetUnit();
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
			return productSystem.getTargetFlowPropertyFactor();
	}

	/**
	 * Optionally set another target amount for the calculation than the one
	 * defined in the product system.
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getAmount() {
		if (amount != null)
			return amount;
		else
			return productSystem.getTargetAmount();
	}
}
