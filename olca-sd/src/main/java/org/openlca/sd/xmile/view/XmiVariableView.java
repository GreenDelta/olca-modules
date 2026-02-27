package org.openlca.sd.xmile.view;

import jakarta.xml.bind.annotation.XmlAttribute;

public abstract class XmiVariableView extends XmiBasicView {

	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "width")
	private Double width;

	@XmlAttribute(name = "height")
	private Double height;

	public String name() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
}
