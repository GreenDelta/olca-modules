
package org.openlca.ilcd.flows;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Availability;
import org.openlca.ilcd.commons.ImpactCategory;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompletenessAvailabilityImpactFactorsType")
public class ImpactFactorAvailability implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlAttribute(name = "type", required = true)
	public ImpactCategory type;

	@XmlAttribute(name = "value", required = true)
	public Availability value;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
