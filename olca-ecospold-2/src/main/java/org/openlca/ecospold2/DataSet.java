package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

public class DataSet {

	private Activity activity;
	private List<Classification> classifications = new ArrayList<>();
	private Geography geography;
	private Technology technology;
	private TimePeriod timePeriod;
	private MacroEconomicScenario macroEconomicScenario;
	private Representativeness representativeness;
	private AdministrativeInformation administrativeInformation;
	private List<ElementaryExchange> elementaryExchanges = new ArrayList<>();
	private List<IntermediateExchange> intermediateExchanges = new ArrayList<>();
	private List<Parameter> parameters = new ArrayList<>();

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public List<Classification> getClassifications() {
		return classifications;
	}

	public Geography getGeography() {
		return geography;
	}

	public void setGeography(Geography geography) {
		this.geography = geography;
	}

	public Technology getTechnology() {
		return technology;
	}

	public void setTechnology(Technology technology) {
		this.technology = technology;
	}

	public TimePeriod getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(TimePeriod timePeriod) {
		this.timePeriod = timePeriod;
	}

	public MacroEconomicScenario getMacroEconomicScenario() {
		return macroEconomicScenario;
	}

	public void setMacroEconomicScenario(
			MacroEconomicScenario macroEconomicScenario) {
		this.macroEconomicScenario = macroEconomicScenario;
	}

	public List<ElementaryExchange> getElementaryExchanges() {
		return elementaryExchanges;
	}

	public List<IntermediateExchange> getIntermediateExchanges() {
		return intermediateExchanges;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public Representativeness getRepresentativeness() {
		return representativeness;
	}

	public void setRepresentativeness(Representativeness representativeness) {
		this.representativeness = representativeness;
	}

	public AdministrativeInformation getAdministrativeInformation() {
		return administrativeInformation;
	}

	public void setAdministrativeInformation(
			AdministrativeInformation administrativeInformation) {
		this.administrativeInformation = administrativeInformation;
	}

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
				dataSet.getElementaryExchanges().add(exchange);
		}
		List<Element> intermediateExchanges = In.childs(flowData,
				"intermediateExchange");
		for (Element e : intermediateExchanges) {
			IntermediateExchange exchange = IntermediateExchange.fromXml(e);
			if (exchange != null)
				dataSet.getIntermediateExchanges().add(exchange);
		}
		List<Element> parameters = In.childs(flowData, "parameter");
		for (Element e : parameters) {
			Parameter p = Parameter.fromXml(e);
			if (p != null)
				dataSet.getParameters().add(p);
		}
	}

	private static void readActivityDescription(Element root, DataSet dataSet) {
		Element description = In.child(root, "activityDescription");
		Element activity = In.child(description, "activity");
		dataSet.setActivity(Activity.fromXml(activity));
		List<Element> classifications = In
				.childs(description, "classification");
		for (Element e : classifications) {
			Classification classification = Classification.fromXml(e);
			dataSet.getClassifications().add(classification);
		}
		dataSet.setGeography(Geography.fromXml(In.child(description,
				"geography")));
		dataSet.setTechnology(Technology.fromXml(In.child(description,
				"technology")));
		dataSet.setTimePeriod(TimePeriod.fromXml(In.child(description,
				"timePeriod")));
		dataSet.setMacroEconomicScenario(MacroEconomicScenario.fromXml(In
				.child(description, "macroEconomicScenario")));
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
		Element root = new Element("ecoSpold", Out.NS);
		Document document = new Document(root);
		Element dataSetElement = Out.addChild(root, "activityDataset");
		Element descriptionElement = Out.addChild(dataSetElement,
				"activityDescription");
		if (activity != null)
			descriptionElement.addContent(activity.toXml());
		for (Classification classification : classifications)
			descriptionElement.addContent(classification.toXml());
		if (geography != null)
			descriptionElement.addContent(geography.toXml());
		if (technology != null)
			descriptionElement.addContent(technology.toXml());
		if (timePeriod != null)
			descriptionElement.addContent(timePeriod.toXml());
		if (macroEconomicScenario != null)
			descriptionElement.addContent(macroEconomicScenario.toXml());
		Element flowData = Out.addChild(dataSetElement, "flowData");
		writeFlowData(flowData);
		Element mav = Out.addChild(dataSetElement, "modellingAndValidation");
		if (representativeness != null)
			mav.addContent(representativeness.toXml());
		if (administrativeInformation != null)
			dataSetElement.addContent(administrativeInformation.toXml());
		return document;
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
