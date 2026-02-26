package org.openlca.sd.xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiElement {

	@XmlAttribute(name = "subscript")
	String subscript;

	@XmlElement(name = "eqn", namespace = Xmile.NS)
	String eqn;

	@XmlElement(name ="gf", namespace = Xmile.NS)
	XmiGf gf;

	@XmlElement(name="non_negative", namespace = Xmile.NS)
	XmiNonNegative nonNegative;

	public String subscript() {
		return subscript;
	}

	public void setSubscript(String subscript) {
		this.subscript = subscript;
	}

	public String eqn() {
		return eqn;
	}

	public void setEqn(String eqn) {
		this.eqn = eqn;
	}

	public XmiGf gf() {
		return gf;
	}

	public void setGf(XmiGf gf) {
		this.gf = gf;
	}

	public boolean isNonNegative() {
		return nonNegative != null;
	}

	public void setNonNegative(XmiNonNegative nonNegative) {
		this.nonNegative = nonNegative;
	}

	public void setNonNegative() {
		this.nonNegative = new XmiNonNegative();
	}
}
