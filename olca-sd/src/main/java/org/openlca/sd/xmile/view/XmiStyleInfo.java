package org.openlca.sd.xmile.view;

import jakarta.xml.bind.annotation.XmlAttribute;

public abstract class XmiStyleInfo {

	@XmlAttribute(name = "color")
	String color;

	@XmlAttribute(name = "background")
	String background;

	@XmlAttribute(name = "font_family")
	String fontFamily;

	@XmlAttribute(name = "font_size")
	String fontSize;

	@XmlAttribute(name = "font_color")
	String fontColor;

	@XmlAttribute(name = "padding")
	Integer padding;

	@XmlAttribute(name = "label_side")
	String labelSide;

	public String color() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String background() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public String fontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public String fontFamily() {
		return fontFamily;
	}

	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public String fontColor() {
		return fontColor;
	}

	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}

	public Integer padding() {
		return padding;
	}

	public void setPadding(Integer padding) {
		this.padding = padding;
	}

	public String labelSide() {
		return labelSide;
	}

	public void setLabelSide(String labelSide) {
		this.labelSide = labelSide;
	}

}
