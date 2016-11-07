
package org.openlca.ilcd.methods;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
