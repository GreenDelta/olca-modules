
package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataQualityIndicatorType")
public class DataQualityIndicator implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlAttribute(name = "name", required = true)
	public QualityIndicator name;

	@XmlAttribute(name = "value", required = true)
	public Quality value;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public DataQualityIndicator clone() {
		DataQualityIndicator clone = new DataQualityIndicator();
		clone.name = name;
		clone.value = value;
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}

}
