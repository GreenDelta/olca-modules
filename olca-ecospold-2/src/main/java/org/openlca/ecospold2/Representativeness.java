package org.openlca.ecospold2;

import org.jdom2.Element;

public class Representativeness {

	private Double percent;
	private String systemModelId;
	private String systemModelName;
	private String samplingProcedure;
	private String extrapolations;

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	public String getSystemModelId() {
		return systemModelId;
	}

	public void setSystemModelId(String systemModelId) {
		this.systemModelId = systemModelId;
	}

	public String getSystemModelName() {
		return systemModelName;
	}

	public void setSystemModelName(String systemModelName) {
		this.systemModelName = systemModelName;
	}

	public String getSamplingProcedure() {
		return samplingProcedure;
	}

	public void setSamplingProcedure(String samplingProcedure) {
		this.samplingProcedure = samplingProcedure;
	}

	public String getExtrapolations() {
		return extrapolations;
	}

	public void setExtrapolations(String extrapolations) {
		this.extrapolations = extrapolations;
	}

	static Representativeness fromXml(Element e) {
		if (e == null)
			return null;
		Representativeness representativeness = new Representativeness();
		String percentStr = e.getAttributeValue("percent");
		if (percentStr != null)
			representativeness.percent = In.decimal(percentStr);
		representativeness.systemModelId = e.getAttributeValue("systemModelId");
		representativeness.systemModelName = In.childText(e, "systemModelName");
		representativeness.samplingProcedure = In.childText(e,
				"samplingProcedure");
		representativeness.extrapolations = In.childText(e, "extrapolations");
		return representativeness;
	}

	Element toXml() {
		Element element = new Element("representativeness", Out.NS);
		if (percent != null)
			element.setAttribute("percent", Double.toString(percent));
		if (systemModelId != null)
			element.setAttribute("systemModelId", systemModelId);
		if (systemModelName != null)
			Out.addChild(element, "systemModelName", systemModelName);
		if (samplingProcedure != null)
			Out.addChild(element, "samplingProcedure", samplingProcedure);
		if (extrapolations != null)
			Out.addChild(element, "extrapolations", extrapolations);
		return element;
	}

}
