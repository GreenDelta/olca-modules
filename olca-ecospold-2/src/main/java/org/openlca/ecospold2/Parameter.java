package org.openlca.ecospold2;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class Parameter {

	public String id;
	public String variableName;
	public double amount;
	public String name;
	public String unitName;
	public String mathematicalRelation;
	public Boolean isCalculatedAmount;

	// extensions
	public String scope;
	public Uncertainty uncertainty;

	static Parameter fromXml(Element e) {
		if (e == null)
			return null;
		Parameter p = new Parameter();
		p.amount = In.decimal(e.getAttributeValue("amount"));
		p.id = e.getAttributeValue("parameterId");
		p.name = In.childText(e, "name");
		p.unitName = In.childText(e, "unitName");
		p.variableName = e.getAttributeValue("variableName");
		p.mathematicalRelation = e.getAttributeValue("mathematicalRelation");
		p.isCalculatedAmount = In.optionalBool(e
				.getAttributeValue("isCalculatedAmount"));
		p.scope = e.getChildText("scope", IO.EXT_NS);
		p.uncertainty = Uncertainty.fromXml(In.child(e, "uncertainty"));
		return p;
	}

	Element toXml() {
		return toXml(IO.NS);
	}

	Element toXml(Namespace ns) {
		Element e = new Element("parameter", ns);
		if (ns == IO.MD_NS)
			e.setAttribute("id", id);
		else
			e.setAttribute("parameterId", id);
		e.setAttribute("amount", Double.toString(amount));
		if (variableName != null)
			e.setAttribute("variableName", variableName);
		if (mathematicalRelation != null)
			e.setAttribute("mathematicalRelation", mathematicalRelation);
		if (isCalculatedAmount != null)
			e.setAttribute("isCalculatedAmount", isCalculatedAmount.toString());
		if (uncertainty != null)
			e.addContent(uncertainty.toXml());
		Out.addChild(e, "name", name);
		Out.addChild(e, "unitName", unitName);
		if (scope != null) {
			Element scopeElement = new Element("scope", IO.EXT_NS);
			scopeElement.setText(scope);
			e.addContent(scopeElement);
		}
		return e;
	}

}
