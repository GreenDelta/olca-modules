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
	public boolean withRegionalization = false;

	/**
	 * Indicates whether the calculation matrices should be created with associated
	 * uncertainty distributions. Typically, this is only required for running Monte
	 * Carlo simulations.
	 */
	public boolean withUncertainties = false;

	public NwSetDescriptor nwSet;
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
	public CalculationSetup(ProductSystem system) {
		this.productSystem = system;
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

	/** Get the target amount in the unit of this calculation setup. */
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
