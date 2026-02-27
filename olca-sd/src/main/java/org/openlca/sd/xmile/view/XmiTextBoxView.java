package org.openlca.sd.xmile.view;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiTextBoxView implements XmiViewPoint {

	@XmlAttribute(name = "uid")
	private String uid;

	@XmlAttribute(name = "x")
	private double x;

	@XmlAttribute(name = "y")
	private double y;

	@XmlAttribute(name = "width")
	private double width;

	@XmlAttribute(name = "height")
	private double height;

	@XmlValue
	private String text;

	public String uid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

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

	public String text() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
