package org.openlca.core.math;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;

/**
 * A setup for a product system calculation.
 */
public class CalculationSetup {

	public final ProductSystem productSystem;
	public ImpactMethodDescriptor impactMethod;
	public boolean withCosts = false;
	public NwSetDescriptor nwSet;
	public AllocationMethod allocationMethod = AllocationMethod.NONE;
	public final List<ParameterRedef> parameterRedefs = new ArrayList<>();

	/** Only valid for Monte Carlo Simulations */
	public int numberOfRuns = -1;

	// properties with default values from the product system
	private Unit unit;
	private FlowPropertyFactor flowPropertyFactor;
	private Double amount;

	public CalculationSetup(ProductSystem productSystem) {
		this.productSystem = productSystem;
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
		double refAmount = productSystem.getTargetAmount();
		if (productSystem.getReferenceExchange() == null)
			return refAmount;
		Flow flow = productSystem.getReferenceExchange().flow;
		if (flow != null && flow.getFlowType() == FlowType.WASTE_FLOW) {
			// negative reference amount for waste treatment processes
			return -refAmount;
		}
		return refAmount;
	}
}
