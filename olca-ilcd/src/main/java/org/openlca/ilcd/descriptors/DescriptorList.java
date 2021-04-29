
package org.openlca.ilcd.descriptors;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
	name = "DataSetListType",
	namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI",
	propOrder = {"descriptors"}
)
public class DescriptorList {

	@XmlElements({
		@XmlElement(name = "LCIAMethod", type = LCIAMethodDescriptor.class,
			namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/LCIAMethod"),
		@XmlElement(name = "process", type = ProcessDescriptor.class,
			namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process"),
		@XmlElement(name = "contact", type = ContactDescriptor.class,
			namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Contact"),
		@XmlElement(name = "source", type = SourceDescriptor.class,
			namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Source"),
		@XmlElement(name = "flow", type = FlowDescriptor.class,
			namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow"),
		@XmlElement(name = "unitGroup", type = UnitGroupDescriptor.class,
			namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/UnitGroup"),
		@XmlElement(name = "flowProperty", type = FlowPropertyDescriptor.class,
			namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/FlowProperty")
	})
	public final List<Descriptor> descriptors = new ArrayList<>();

	@XmlAttribute(
		name = "totalSize",
		namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public int totalSize;

	@XmlAttribute(
		name = "startIndex",
		namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public int startIndex;

	@XmlAttribute(
		name = "pageSize",
		namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public int pageSize;

}
