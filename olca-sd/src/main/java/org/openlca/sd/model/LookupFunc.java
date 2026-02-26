package org.openlca.sd.model;

import java.util.Objects;

/// A lookup function (table function) like defined in the XMILE standard.
public class LookupFunc {

	public enum Type {
		CONTINUOUS,
		EXTRAPOLATE,
		DISCRETE
	}

	private final Type type;
	private final int n;
	private final double[] xs;
	private final double[] ys;

	public LookupFunc(Type type, double[] xs, double[] ys) {
		this.type = Objects.requireNonNullElse(type, Type.EXTRAPOLATE);
		this.xs = xs != null ? xs : new double[0];
		this.ys = ys != null ? ys : new double[0];
		this.n = Math.min(this.xs.length, this.ys.length);
	}

	public Type type() {
		return type;
	}

	public double[] xs() {
		return xs;
	}

	public double[] ys() {
		return ys;
	}

	public LookupFunc(Type type, double start, double end, double[] ys) {
		this(type, scale(start, end, ys != null ? ys.length : 0), ys);
	}

	private static double[] scale(double start, double end, int n) {
		if (n <= 0)
			return new double[0];
		if (n == 1)
			return new double[]{(start + end) / 2};
		if (n == 2)
			return new double[]{start, end};

		double step = (end - start) / (n - 1);
		double[] xs = new double[n];
		for (int i = 0; i < n; i++) {
			xs[i] = start + i * step;
		}
		return xs;
	}

	public double get(double x) {
		if (n == 0)
			return 0;
		if (n == 1)
			return ys[0];

		if (x < xs[0]) {
			return switch (type) {
				case CONTINUOUS, DISCRETE -> ys[0];
				case EXTRAPOLATE -> getY(x, 0, 1);
			};
		}

		if (x > xs[n - 1]) {
			return switch (type) {
				case CONTINUOUS, DISCRETE -> ys[n - 1];
				case EXTRAPOLATE -> getY(x, n - 2, n - 1);
			};
		}

		for (int i = 0; i < n - 1; i++) {
			if (x == xs[i])
				return ys[i];
			int j = i + 1;
			if (x < xs[j]) {
				return switch (type) {
					case CONTINUOUS, EXTRAPOLATE -> getY(x, i, j);
					case DISCRETE -> ys[i];
				};
			}
		}
		return ys[n - 1];
	}

	/// Calculates the value `y` for a given x and two points `i` and `j`.
	/// This is the same formula for linear interpolation and extrapolation:
	/// ```
	///(y - yi) / (x - xi) = (yj - yi) / (xj - xi)
	///   y - yi = (x - xi) * (yj - yi) / (xj - xi)
	///   y = yi + (x - xi) * (yj - yi) / (xj - xi)
	///```
	private double getY(double x, int i, int j) {
		double xi = xs[i];
		double xj = xs[j];
		if (xi == xj)
			return ys[i];
		double yi = ys[i];
		double yj = ys[j];
		return yi + (x - xi) * (yj - yi) / (xj - xi);
	}
}
