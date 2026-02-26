package org.openlca.sd.xmile.view;

import jakarta.xml.bind.annotation.XmlAttribute;

abstract class XmiBasicView extends XmiStyleInfo implements XmiViewPoint {

	@XmlAttribute(name = "x")
	double x;

	@XmlAttribute(name = "y")
	double y;

	public double x() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double y() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
}
