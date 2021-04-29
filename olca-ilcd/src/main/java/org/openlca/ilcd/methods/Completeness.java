package org.openlca.ilcd.methods;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompletenessType", propOrder = {
		"impactCoverage",
		"inventoryItems"
})
public class Completeness implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "completenessImpactCoverage")
	public Double impactCoverage;
	public Integer inventoryItems;

}
