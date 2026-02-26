package org.openlca.sd.xmile.svg;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class SvgPath {

	@XmlAttribute
	String d;

	@XmlAttribute
	String stroke;

	@XmlAttribute
	String fill;

	@XmlAttribute(name = "stroke-width")
	double strokeWidth;

	public SvgPath(String d, String stroke) {
		this.d = d;
		this.stroke = stroke;
		this.fill = "none";
		this.strokeWidth = 2.0;
	}

	public SvgPath() {}
}
