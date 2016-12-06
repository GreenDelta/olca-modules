package org.openlca.ecospold2;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class Classification {

	public String classificationId;
	public String classificationSystem;
	public String classificationValue;

	static Classification fromXml(Element element) {
		if (element == null)
			return null;
		Classification classification = new Classification();
		classification.classificationId = element
		.getAttributeValue("classificationId");
		classification.classificationSystem = In.childText(element,
		"classificationSystem");
		classification.classificationValue = In.childText(element,
		"classificationValue");
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
