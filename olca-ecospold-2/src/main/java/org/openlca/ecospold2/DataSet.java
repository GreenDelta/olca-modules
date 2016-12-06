package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.jdom2.Document;
import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataSet {

	@XmlElement(name = "activityDescription")
	public ActivityDescription description;

	public Representativeness representativeness;
	public AdministrativeInformation administrativeInformation;
	public final List<ElementaryExchange> elementaryExchanges = new ArrayList<>();
	public final List<IntermediateExchange> intermediateExchanges = new ArrayList<>();
	public final List<Parameter> parameters = new ArrayList<>();
	public UserMasterData masterData;

	static DataSet fromXml(Document doc) {
		Element root = getRootElement(doc);
		if (root == null)
			return null;
		DataSet dataSet = new DataSet();
		readActivityDescription(root, dataSet);
		readFlowData(root, dataSet);
		dataSet.representativeness = Representativeness.fromXml(In.child(root,
				"modellingAndValidation", "representativeness"));
		dataSet.administrativeInformation = AdministrativeInformation
				.fromXml(In.child(root, "administrativeInformation"));
		return dataSet;
	}

	private static void readFlowData(Element root, DataSet dataSet) {
		Element flowData = In.child(root, "flowData");
		if (flowData == null)
			return;
		List<Element> elementaryExchanges = In.childs(flowData,
				"elementaryExchange");
		for (Element e : elementaryExchanges) {
			ElementaryExchange exchange = ElementaryExchange.fromXml(e);
			if (exchange != null)
				dataSet.elementaryExchanges.add(exchange);
		}
		List<Element> intermediateExchanges = In.childs(flowData,
				"intermediateExchange");
		for (Element e : intermediateExchanges) {
			IntermediateExchange exchange = IntermediateExchange.fromXml(e);
			if (exchange != null)
				dataSet.intermediateExchanges.add(exchange);
		}
		List<Element> parameters = In.childs(flowData, "parameter");
		for (Element e : parameters) {
			Parameter p = Parameter.fromXml(e);
			if (p != null)
				dataSet.parameters.add(p);
		}
	}

	private static void readActivityDescription(Element root, DataSet dataSet) {
		Element description = In.child(root, "activityDescription");
		Element activity = In.child(description, "activity");
		ActivityDescription d = new ActivityDescription();
		dataSet.description = d;
		d.activity = Activity.fromXml(activity);
		List<Element> classifications = In
				.childs(description, "classification");
		for (Element e : classifications) {
			Classification classification = Classification.fromXml(e);
			d.classifications.add(classification);
		}
		d.geography = Geography.fromXml(In.child(description,
				"geography"));
		d.technology = Technology.fromXml(In.child(description,
				"technology"));
		d.timePeriod = TimePeriod.fromXml(In.child(description,
				"timePeriod"));
		d.macroEconomicScenario = MacroEconomicScenario.fromXml(In
				.child(description, "macroEconomicScenario"));
	}

	private static Element getRootElement(Document doc) {
		if (doc == null)
			return null;
		Element root = doc.getRootElement();
		if (!"ecoSpold".equals(root.getName()))
			return null;
		Element e = root.getChild("activityDataset", root.getNamespace());
		if (e == null)
			e = root.getChild("childActivityDataset", root.getNamespace());
		return e;
	}

	Document toXml() {
		Element root = new Element("ecoSpold", IO.NS);
		Document document = new Document(root);
		Element dataSetElement = Out.addChild(root, "activityDataset");
		Element descriptionElement = Out.addChild(dataSetElement,
				"activityDescription");
		writeDescription(descriptionElement);
		Element flowData = Out.addChild(dataSetElement, "flowData");
		writeFlowData(flowData);
		Element mav = Out.addChild(dataSetElement, "modellingAndValidation");
		if (representativeness != null)
			mav.addContent(representativeness.toXml());
		if (administrativeInformation != null)
			dataSetElement.addContent(administrativeInformation.toXml());
		if (masterData != null)
			root.addContent(masterData.toXml());
		return document;
	}

	private void writeDescription(Element descriptionElement) {
		if (description == null)
			return;
		ActivityDescription d = description;
		if (d.activity != null)
			descriptionElement.addContent(d.activity.toXml());
		for (Classification classification : d.classifications)
			descriptionElement.addContent(classification.toXml());
		if (d.geography != null)
			descriptionElement.addContent(d.geography.toXml());
		if (d.technology != null)
			descriptionElement.addContent(d.technology.toXml());
		if (d.timePeriod != null)
			descriptionElement.addContent(d.timePeriod.toXml());
		if (d.macroEconomicScenario != null)
			descriptionElement.addContent(d.macroEconomicScenario.toXml());
	}

	private void writeFlowData(Element flowData) {
		for (IntermediateExchange exchange : intermediateExchanges)
			flowData.addContent(exchange.toXml());
		for (ElementaryExchange exchange : elementaryExchanges)
			flowData.addContent(exchange.toXml());
		for (Parameter parameter : parameters)
			flowData.addContent(parameter.toXml());
	}

}
