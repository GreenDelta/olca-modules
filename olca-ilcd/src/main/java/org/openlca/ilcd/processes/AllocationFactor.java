package org.openlca.ilcd.processes;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AllocationType")
public class AllocationFactor implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlAttribute(name = "internalReferenceToCoProduct")
	public int productExchangeId;

	@XmlAttribute(name = "allocatedFraction")
	public double fraction;

	@Override
	public AllocationFactor clone() {
		AllocationFactor clone = new AllocationFactor();
		clone.productExchangeId = productExchangeId;
		clone.fraction = fraction;
		return clone;
	}
}
