package org.openlca.sd.xmile.lca;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiVarBinding {

	@XmlAttribute(name = "var")
	private String variable;

	@XmlAttribute(name = "parameter")
	private String parameter;

	@XmlElement(name = "context", namespace = XmiLca.NS)
	private XmiEntityRef context;

	public String variable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public String parameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public XmiEntityRef context() {
		return context;
	}

	public void setContext(XmiEntityRef context) {
		this.context = context;
	}

}
