
package org.openlca.ilcd.descriptors;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.LangString;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"copyright",
		"licenseType",
		"useRestrictions"
})
@XmlRootElement(name = "accessInformation")
public class AccessInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	public Boolean copyright;
	public String licenseType;
	public LangString useRestrictions;

}
