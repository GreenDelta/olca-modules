package org.openlca.sd.xmile.extensions;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.openlca.core.model.AllocationMethod;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SystemBinding", namespace = XmiLca.NS)
public class XmiSystemBinding {

	@XmlElement(name = "system", namespace = XmiLca.NS)
	private XmiEntityRef system;

	@XmlAttribute(name = "allocation")
	private AllocationMethod allocation;

	@XmlAttribute(name = "amount")
	private double amount = 1.0;

	@XmlAttribute(name = "amountVar")
	private String amountVar;

	@XmlElement(name = "varBinding", namespace = XmiLca.NS)
	private List<XmiVarBinding> varBindings;

	public XmiEntityRef system() {
		return system;
	}

	public void setSystem(XmiEntityRef system) {
		this.system = system;
	}

	public AllocationMethod allocation() {
		return allocation;
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

	public String amountVar() {
		return amountVar;
	}

	public void setAmountVar(String amountVar) {
		this.amountVar = amountVar;
	}

	public List<XmiVarBinding> varBindings() {
		if (varBindings == null) {
			varBindings = new ArrayList<>();
		}
		return varBindings;
	}
}
