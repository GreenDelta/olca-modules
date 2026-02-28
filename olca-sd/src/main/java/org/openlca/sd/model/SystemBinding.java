package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.AllocationMethod;

public class SystemBinding {

	private final EntityRef system;
	private AllocationMethod allocation;
	private double amount = 1.0;
	private Id amountVar;
	private final List<VarBinding> varBindings = new ArrayList<>();

	public SystemBinding(EntityRef system) {
		this.system = system;
	}

	public EntityRef system() {
		return system;
	}

	public AllocationMethod allocation() {
		return allocation != null
			? allocation
			: AllocationMethod.USE_DEFAULT;
	}

	public void setAllocation(AllocationMethod allocation) {
		this.allocation = allocation;
	}

	public double amount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Id amountVar() {
		return amountVar;
	}

	public void setAmountVar(Id amountVar) {
		this.amountVar = amountVar;
	}

	public List<VarBinding> varBindings() {
		return varBindings;
	}

	@Override
	public String toString() {
		if (system == null) return super.toString();
		var id = system.refId() != null && system.refId().length() >= 5
			? system.refId().substring(0, 5)
			: system.refId();
		return system.name() + "@" + id;
	}
}
