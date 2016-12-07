package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.jdom2.Element;
import org.jdom2.Namespace;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
		"name",
		"unit",
		"comment",
		"uncertainty",
		"properties",
		"classifications",
		"inputGroup",
		"outputGroup"
})
public class IntermediateExchange extends Exchange {

	@XmlAttribute(name = "intermediateExchangeId")
	public String flowId;

	@XmlAttribute
	public String activityLinkId;

	@XmlAttribute
	public Double productionVolumeAmount;

	@XmlAttribute
	public String productionVolumeVariableName;

	@XmlAttribute
	public String productionVolumeMathematicalRelation;

	@XmlElement(name = "classification")
	public final List<Classification> classifications = new ArrayList<>();

	static IntermediateExchange fromXml(Element e) {
		if (e == null)
			return null;
		IntermediateExchange ie = new IntermediateExchange();
		ie.readValues(e);
		ie.activityLinkId = e.getAttributeValue("activityLinkId");
		ie.flowId = e.getAttributeValue("intermediateExchangeId");
		ie.productionVolumeAmount = In.optionalDecimal(e.getAttributeValue("productionVolumeAmount"));
		ie.productionVolumeMathematicalRelation = e.getAttributeValue("productionVolumeMathematicalRelation");
		ie.productionVolumeVariableName = e.getAttributeValue("productionVolumeVariableName");
		List<Element> classElements = In.childs(e, "classification");
		for (Element classElement : classElements) {
			Classification classification = Classification.fromXml(classElement);
			ie.classifications.add(classification);
		}
		return ie;
	}

	Element toXml() {
		return toXml(IO.NS);
	}

	Element toXml(Namespace ns) {
		Element e = new Element("intermediateExchange", ns);
		if (flowId != null)
			e.setAttribute("intermediateExchangeId", flowId);
		if (activityLinkId != null)
			e.setAttribute("activityLinkId", activityLinkId);
		if (productionVolumeAmount != null)
			e.setAttribute("productionVolumeAmount", productionVolumeAmount.toString());
		if (productionVolumeMathematicalRelation != null)
			e.setAttribute("productionVolumeMathematicalRelation", productionVolumeMathematicalRelation);
		if (productionVolumeVariableName != null)
			e.setAttribute("productionVolumeVariableName", productionVolumeVariableName);
		writeValues(e);
		for (Classification classification : classifications)
			e.addContent(classification.toXml(ns));
		writeInputOutputGroup(e);
		return e;
	}

}
