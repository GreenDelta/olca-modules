
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AllocationType")
public class AllocationFactor implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlAttribute(name = "internalReferenceToCoProduct")
	public BigInteger referenceToCoProduct;

	@XmlAttribute(name = "allocatedFraction")
	public BigDecimal allocatedFraction;

}
