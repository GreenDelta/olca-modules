package org.openlca.sd.model;

public class SimSpecs {

	private double start;
	private double end;
	private double dt;
	private String unit;

	public SimSpecs() {
	}

	public SimSpecs(double start, double end, double dt, String unit) {
		this.start = start;
		this.end = end;
		this.dt = dt;
		this.unit = unit;
	}

	public SimSpecs(double start, double end, String unit) {
		this(start, end, 1, unit);
	}

	public double start() {
		return start;
	}

	public void setStart(double start) {
		this.start = start;
	}

	public double end() {
		return end;
	}

	public void setEnd(double end) {
		this.end = end;
	}

	public double dt() {
		return dt;
	}

	public void setDt(double dt) {
		this.dt = dt;
	}

	public String unit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public int iterationCount() {
		if (dt == 0)
			return 0;
		double count = 1 + (end - start) / dt;
		return (int) count;
	}
}
