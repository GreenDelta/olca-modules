
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.FlowCompleteness;
import org.openlca.ilcd.commons.ImpactCategory;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompletenessElementaryFlowsType")
public class FlowCompletenessEntry implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlAttribute(name = "type")
	public ImpactCategory impact;

	@XmlAttribute(name = "value")
	public FlowCompleteness value;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public FlowCompletenessEntry clone() {
		FlowCompletenessEntry clone = new FlowCompletenessEntry();
		clone.impact = impact;
		clone.value = value;
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
