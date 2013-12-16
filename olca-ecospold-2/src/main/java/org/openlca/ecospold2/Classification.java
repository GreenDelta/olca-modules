package org.openlca.ecospold2;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class Classification {

	private String classificationId;
	private String classificationSystem;
	private String classificationValue;

	public String getClassificationId() {
		return classificationId;
	}

	public void setClassificationId(String classificationId) {
		this.classificationId = classificationId;
	}

	public String getClassificationSystem() {
		return classificationSystem;
	}

	public void setClassificationSystem(String classificationSystem) {
		this.classificationSystem = classificationSystem;
	}

	public String getClassificationValue() {
		return classificationValue;
	}

	public void setClassificationValue(String classificationValue) {
		this.classificationValue = classificationValue;
	}

	static Classification fromXml(Element element) {
		if (element == null)
			return null;
		Classification classification = new Classification();
		classification.setClassificationId(element
				.getAttributeValue("classificationId"));
		classification.setClassificationSystem(In.childText(element,
				"classificationSystem"));
		classification.setClassificationValue(In.childText(element,
				"classificationValue"));
		return classification;
	}

	Element toXml() {
		return toXml(IO.NS);
	}

	Element toXml(Namespace ns) {
		Element element = new Element("classification", ns);
		element.setAttribute("classificationId", classificationId);
		Out.addChild(element, "classificationSystem", classificationSystem);
		Out.addChild(element, "classificationValue", classificationValue);
		return element;
	}

}
