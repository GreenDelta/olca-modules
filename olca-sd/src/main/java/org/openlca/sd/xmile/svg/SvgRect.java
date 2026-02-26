package org.openlca.sd.xmile.svg;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class SvgRect {

	@XmlAttribute
	double x;

	@XmlAttribute
	double y;

	@XmlAttribute
	double width;

	@XmlAttribute
	double height;

	@XmlAttribute
	String fill;

	@XmlAttribute
	String stroke;

	@XmlAttribute(name = "stroke-width")
	double strokeWidth;

	public SvgRect(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.stroke = "black";
		this.strokeWidth = 1.0;
	}

	public SvgRect() {}
}
