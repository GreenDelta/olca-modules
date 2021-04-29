
package org.openlca.ilcd.descriptors;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"referenceYear",
		"validUntil"
})
@XmlRootElement(name = "time")
public class Time implements Serializable {

	private final static long serialVersionUID = 1L;

	public Integer referenceYear;
	public Integer validUntil;

}
