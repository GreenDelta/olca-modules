package org.openlca.sd.xmile.svg;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class SvgText {

	@XmlAttribute
	double x;

	@XmlAttribute
	double y;

	@XmlAttribute
	String fill;

	@XmlAttribute(name = "font-family")
	String fontFamily;

	@XmlAttribute(name = "font-size")
	double fontSize;

	@XmlAttribute(name = "text-anchor")
	String textAnchor;

	@XmlElement(name = "tspan", namespace = Svg.NS)
	List<Span> spans;

	public SvgText(double x, double y) {
		this.x = x;
		this.y = y;
		this.spans = new ArrayList<>();
	}

	public void addSpan(Span span) {
		if (spans == null) {
			spans = new ArrayList<>();
		}
		spans.add(span);
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Span {

		@XmlAttribute
		Double x;

		@XmlAttribute
		Double y;

		@XmlAttribute
		Double dx;

		@XmlAttribute
		Double dy;

		@XmlValue
		String content;

		public Span(double x, double dy, String content) {
			this.x = x;
			this.dy = dy;
			this.content = content;
		}

		public Span(String content) {
			this.content = content;
		}

		public Span() {
		}
	}
}
