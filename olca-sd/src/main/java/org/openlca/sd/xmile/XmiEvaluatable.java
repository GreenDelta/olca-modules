package org.openlca.sd.xmile;

import java.util.ArrayList;
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
	private String eqn;

	@XmlElement(name = "units", namespace = Xmile.NS)
	private String units;

	@XmlElement(name = "doc", namespace = Xmile.NS)
	private String doc;

	@XmlElement(name ="gf", namespace = Xmile.NS)
	private XmiGf gf;

	@XmlElementWrapper(name = "dimensions", namespace = Xmile.NS)
	@XmlElement(name="dim", namespace = Xmile.NS)
	private List<Dim> dimensions;

	@XmlElement(name = "element", namespace = Xmile.NS)
	private List<XmiElement> elements;

	@XmlElement(name = "non_negative", namespace = Xmile.NS)
	private XmiNonNegative nonNegative;

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
		if (dimensions == null) {
			dimensions = new ArrayList<>();
		}
		return dimensions;
	}

	public List<XmiElement> elements() {
		if (elements == null) {
			elements = new ArrayList<>();
		}
		return elements;
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
		private String name;

		public String name() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
