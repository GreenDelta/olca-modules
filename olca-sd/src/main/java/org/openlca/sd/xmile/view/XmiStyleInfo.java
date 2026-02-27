package org.openlca.sd.xmile.view;

import jakarta.xml.bind.annotation.XmlAttribute;

public abstract class XmiStyleInfo {

	@XmlAttribute(name = "color")
	private String color;

	@XmlAttribute(name = "background")
	private String background;

	@XmlAttribute(name = "font_family")
	private String fontFamily;

	@XmlAttribute(name = "font_size")
	private String fontSize;

	@XmlAttribute(name = "font_color")
	private String fontColor;

	@XmlAttribute(name = "padding")
	private Integer padding;

	@XmlAttribute(name = "label_side")
	private String labelSide;

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
