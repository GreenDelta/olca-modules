
package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSetListType", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", propOrder = {
		"descriptors"
})
public class DescriptorList implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElements({
			@XmlElement(name = "process", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", type = ProcessDescriptor.class),
			@XmlElement(name = "contact", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Contact", type = ContactDescriptor.class),
			@XmlElement(name = "source", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Source", type = SourceDescriptor.class),
			@XmlElement(name = "flow", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow", type = FlowDescriptor.class),
			@XmlElement(name = "unitGroup", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/UnitGroup", type = UnitGroupDescriptor.class),
			@XmlElement(name = "flowProperty", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/FlowProperty", type = FlowPropertyDescriptor.class)
	})
	public final List<Descriptor> descriptors = new ArrayList<>();

	@XmlAttribute(name = "sourceId", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String sourceId;

}
