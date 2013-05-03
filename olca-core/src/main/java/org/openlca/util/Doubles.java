package org.openlca.util;

public class Doubles {

	public static double min(double[] values) {
		if (values == null || values.length == 0)
			return Double.NaN;
		double min = values[0];
		for (int i = 1; i < values.length; i++)
			min = Math.min(min, values[i]);
		return min;
	}

	public static double max(double[] values) {
		if (values == null || values.length == 0)
			return Double.NaN;
		double max = values[0];
		for (int i = 1; i < values.length; i++)
			max = Math.max(max, values[i]);
		return max;
	}

	public static double sum(double[] values) {
		if (values == null)
			return 0;
		double sum = 0;
		for (double val : values)
			sum += val;
		return sum;
	}

}
