package org.openlca.sd.xmile.svg;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "svg", namespace = Svg.NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class SvgDoc {

	@XmlAttribute
	double width;

	@XmlAttribute
	double height;

	@XmlElement(name = "rect", namespace = Svg.NS)
	List<SvgRect> rectangles;

	@XmlElement(name = "text", namespace = Svg.NS)
	List<SvgText> texts;

	@XmlElement(name = "line", namespace = Svg.NS)
	List<SvgLine> lines;

	@XmlElement(name = "path", namespace = Svg.NS)
	List<SvgPath> paths;

	public SvgDoc(double width, double height) {
		this.width = width;
		this.height = height;
		this.rectangles = new ArrayList<>();
		this.texts = new ArrayList<>();
		this.lines = new ArrayList<>();
		this.paths = new ArrayList<>();
	}

	public SvgDoc() {
		this(800, 600);
	}

	public void addRect(SvgRect rect) {
		rectangles.add(rect);
	}

	public void addText(SvgText text) {
		texts.add(text);
	}

	public void addLine(SvgLine line) {
		lines.add(line);
	}

	public void addPath(SvgPath path) {
		paths.add(path);
	}
}
