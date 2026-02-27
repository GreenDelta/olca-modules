package org.openlca.sd.xmile.view;

import java.util.ArrayList;
import java.util.List;

import org.openlca.sd.xmile.Xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiFlowView extends XmiVariableView {

	@XmlElementWrapper(name = "pts", namespace = Xmile.NS)
	@XmlElement(name = "pt", namespace = Xmile.NS)
	private List<Pt> pts;

	public List<Pt> pts() {
		if (pts == null) {
			pts = new ArrayList<>();
		}
		return pts;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Pt implements XmiViewPoint {

		@XmlAttribute(name = "x")
		private double x;

		@XmlAttribute(name = "y")
		private double y;

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
	}
}
