package org.openlca.core.matrix.format;

public class Numbers {

	public static boolean isZero(double val) {
		return Math.abs(val) < 1e-25;
	}

}
