package org.openlca.ilcd.flows;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlowPropertiesType", propOrder = {
		"flowProperties"
})
public class FlowPropertyList {

	@XmlElement(required = true, name = "flowProperty")
	public final List<FlowPropertyRef> flowProperties = new ArrayList<>();

}