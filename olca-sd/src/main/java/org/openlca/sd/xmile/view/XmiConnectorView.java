package org.openlca.sd.xmile.view;

import org.openlca.sd.xmile.Xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiConnectorView {

	@XmlAttribute(name = "uid")
	private String uid;

	@XmlAttribute(name = "angle")
	private double angle;

	@XmlElement(name = "from", namespace = Xmile.NS)
	private String from;

	@XmlElement(name = "to", namespace = Xmile.NS)
	private String to;

	public String uid() {
		return uid;
	}

	public double angle() {
		return angle;
	}

	public String from() {
		return from;
	}

	public String to() {
		return to;
	}
}
