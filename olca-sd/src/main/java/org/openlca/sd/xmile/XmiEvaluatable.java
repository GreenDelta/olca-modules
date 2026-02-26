package org.openlca.sd.xmile;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

public sealed abstract class XmiEvaluatable
	extends XmiVariable
	permits XmiAux, XmiFlow, XmiStock {

	@XmlElement(name = "eqn", namespace = Xmile.NS)
	String eqn;

	@XmlElement(name = "units", namespace = Xmile.NS)
	String units;

	@XmlElement(name = "doc", namespace = Xmile.NS)
	String doc;

	@XmlElement(name ="gf", namespace = Xmile.NS)
	XmiGf gf;

	@XmlElementWrapper(name = "dimensions", namespace = Xmile.NS)
	@XmlElement(name="dim", namespace = Xmile.NS)
	List<Dim> dimensions;

	@XmlElement(name = "element", namespace = Xmile.NS)
	List<XmiElement> elements;

	@XmlElement(name = "non_negative", namespace = Xmile.NS)
	XmiNonNegative nonNegative;

	public String eqn() {
		return eqn;
	}

	public void setEqn(String eqn) {
		this.eqn = eqn;
	}

	public String units() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String doc() {
		return doc;
	}

	public void setDoc(String doc) {
		this.doc = doc;
	}

	public XmiGf gf() {
		return gf;
	}

	public void setGf(XmiGf gf) {
		this.gf = gf;
	}

	public List<Dim> dimensions() {
		return dimensions == null ? List.of() : dimensions;
	}

	public void setDimensions(List<Dim> dimensions) {
		this.dimensions = dimensions;
	}

	public List<XmiElement> elements() {
		return elements != null ? elements : List.of();
	}

	public void setElements(List<XmiElement> elements) {
		this.elements = elements;
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

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Dim {

		@XmlAttribute(name = "name")
		String name;

		public String name() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
