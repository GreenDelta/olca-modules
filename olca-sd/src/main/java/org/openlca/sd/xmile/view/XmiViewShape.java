package org.openlca.sd.xmile.view;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiViewShape {

	@XmlAttribute(name = "type")
	private String type;

	@XmlAttribute(name = "width")
	private Double width;

	@XmlAttribute(name = "height")
	private Double height;

	@XmlAttribute(name = "radius")
	private Double radius;

	public String type() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double width() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public Double height() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public Double radius() {
		return radius;
	}

	public void setRadius(Double radius) {
		this.radius = radius;
	}
}
