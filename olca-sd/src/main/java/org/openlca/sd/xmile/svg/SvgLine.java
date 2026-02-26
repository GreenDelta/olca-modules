package org.openlca.sd.xmile.svg;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class SvgLine {

	@XmlAttribute
	double x1;

	@XmlAttribute
	double y1;

	@XmlAttribute
	double x2;

	@XmlAttribute
	double y2;

	@XmlAttribute
	String stroke;

	@XmlAttribute(name = "stroke-width")
	double strokeWidth;

	public SvgLine(double x1, double y1, double x2, double y2, String stroke) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.stroke = stroke;
		this.strokeWidth = 1.0;
	}

	public SvgLine() {}
}
