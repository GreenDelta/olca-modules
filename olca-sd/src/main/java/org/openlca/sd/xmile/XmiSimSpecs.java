package org.openlca.sd.xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiSimSpecs {

	@XmlAttribute(name = "method")
	private String method;

	@XmlAttribute(name = "time_units")
	private String timeUnits;

	@XmlElement(name = "start", namespace = Xmile.NS)
	private Double start;

	@XmlElement(name = "stop", namespace = Xmile.NS)
	private Double stop;

	@XmlElement(name = "dt", namespace = Xmile.NS)
	private DeltaT dt;

	public String method() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String timeUnits() {
		return timeUnits;
	}

	public void setTimeUnits(String timeUnits) {
		this.timeUnits = timeUnits;
	}

	public Double start() {
		return start;
	}

	public void setStart(Double start) {
		this.start = start;
	}

	public Double stop() {
		return stop;
	}

	public void setStop(Double stop) {
		this.stop = stop;
	}

	public DeltaT dt() {
		return dt;
	}

	public void setDt(DeltaT dt) {
		this.dt = dt;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class DeltaT {

		@XmlAttribute(name = "reciprocal")
		private Boolean reciprocal;

		@XmlValue
		private Double value;

		public Boolean reciprocal() {
			return reciprocal;
		}

		public void setReciprocal(Boolean reciprocal) {
			this.reciprocal = reciprocal;
		}

		public Double value() {
			return value;
		}

		public void setValue(Double value) {
			this.value = value;
		}

		public boolean isReciprocal() {
			return reciprocal != null && reciprocal;
		}
	}
}
