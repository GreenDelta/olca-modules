
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataGeneratorType", propOrder = {
		"contacts",
		"other"
})
public class DataGenerator implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToPersonOrEntityGeneratingTheDataSet")
	public final List<Ref> contacts = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public DataGenerator clone() {
		DataGenerator clone = new DataGenerator();
		Ref.copy(contacts, clone.contacts);
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
