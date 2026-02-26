package org.openlca.sd.xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiMinMax {

	@XmlAttribute(name = "min")
	double min;

	@XmlAttribute(name = "max")
	double max;

	public double min() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double max() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}
}
