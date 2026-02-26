package org.openlca.sd.xmile.view;

import org.openlca.sd.xmile.Xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiElementStyle extends XmiStyleInfo {

	@XmlElement(name = "shape", namespace = Xmile.NS)
	XmiViewShape shape;

	public XmiViewShape shape() {
		return shape;
	}

	public void setShape(XmiViewShape shape) {
		this.shape = shape;
	}
}
