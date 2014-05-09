package org.openlca.eigen;

public class Numbers {

	public static boolean isZero(double val) {
		return Math.abs(val) < 1e-25;
	}

}
