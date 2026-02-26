package org.openlca.sd.xmile.view;

import org.openlca.sd.xmile.Xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiConnectorView {

	@XmlAttribute(name = "uid")
	String uid;

	@XmlAttribute(name = "angle")
	double angle;

	@XmlElement(name = "from", namespace = Xmile.NS)
	String from;

	@XmlElement(name = "to", namespace = Xmile.NS)
	String to;

	public String uid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public double angle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public String from() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String to() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
}
