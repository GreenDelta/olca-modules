package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.IMatrix;

import java.io.File;

public final class Npy {

	private Npy() {
	}

	public static IMatrix load(File file) {
		return null;
	}

	public static void save(File file, IMatrix matrix) {
		if (file == null || matrix == null)
			return;

	}

}
