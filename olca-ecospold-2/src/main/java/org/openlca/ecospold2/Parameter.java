package org.openlca.ecospold2;

import org.jdom2.Element;

public class Parameter {

	private String id;
	private String variableName;
	private double amount;
	private String name;
	private String unitName;
	private String mathematicalRelation;
	private Boolean isCalculatedAmount;

	// extensions
	private String scope;
	private Uncertainty uncertainty;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getMathematicalRelation() {
		return mathematicalRelation;
	}

	public void setMathematicalRelation(String mathematicalRelation) {
		this.mathematicalRelation = mathematicalRelation;
	}

	public void setIsCalculatedAmount(Boolean isCalculatedAmount) {
		this.isCalculatedAmount = isCalculatedAmount;
	}

	public Boolean getIsCalculatedAmount() {
		return isCalculatedAmount;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getScope() {
		return scope;
	}

	public Uncertainty getUncertainty() {
		return uncertainty;
	}

	public void setUncertainty(Uncertainty uncertainty) {
		this.uncertainty = uncertainty;
	}

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
		Element e = new Element("parameter", IO.NS);
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
