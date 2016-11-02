
package org.openlca.ilcd.descriptors;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
