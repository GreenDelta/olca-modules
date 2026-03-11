package org.openlca.sd.xmile.lca;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiRect {

	@XmlAttribute(name = "x")
	private int x;

	@XmlAttribute(name = "y")
	private int y;

	@XmlAttribute(name = "width")
	private int width;

	@XmlAttribute(name = "height")
	private int height;

	public int x() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int y() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int width() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int height() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
