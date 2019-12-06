package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.IMatrix;

import java.io.File;

public final class Npy {

	private Npy() {
	}

	public static IMatrix load(File file) {
		Header header = Header.read(file);
		int[] shape = header.shape;
		if (shape == null
				|| shape.length != 2
				|| shape[0] < 1
				|| shape[1] < 1) {
			throw new IllegalArgumentException(
					"invalid header shape " + header + ": " + file);
		}
		if (!"<f8".equals(header.dtype)) {
			throw new IllegalArgumentException(
					"unknown data type: " + header.dtype + ": " + file);
		}
		// TODO: handle C order ...
		return new DenseReader(file, header).run();
	}

	public static void save(File file, IMatrix matrix) {
		if (file == null || matrix == null)
			return;
		new DenseWriter(file, matrix).run();
	}
}
