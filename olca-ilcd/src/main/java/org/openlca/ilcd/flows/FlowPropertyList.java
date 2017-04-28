package org.openlca.ilcd.flows;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlowPropertiesType", propOrder = {
		"flowProperties"
})
public class FlowPropertyList {

	@XmlElement(required = true, name = "flowProperty")
	public final List<FlowPropertyRef> flowProperties = new ArrayList<>();

}