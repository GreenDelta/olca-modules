package org.openlca.sd.xmile.view;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiAuxView extends XmiVariableView {

	@XmlAttribute(name = "width")
	double width;

	@XmlAttribute(name = "height")
	double height;

	public double width() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double height() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}
}
