
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LCIAMethodInformationType", propOrder = {
		"dataSetInfo",
		"quantitativeReference",
		"time",
		"geography",
		"impactModel",
		"other"
})
public class MethodInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "dataSetInformation", required = true)
	public DataSetInfo dataSetInfo;

	public QuantitativeReference quantitativeReference;

	public Time time;

	@XmlElement(required = true)
	public Geography geography;

	public ImpactModel impactModel;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
