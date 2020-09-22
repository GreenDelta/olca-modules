package org.openlca.core.library;

import java.io.File;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.io.npy.Npy;
import org.openlca.core.matrix.io.npy.Npz;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

class MatrixDataExport {

	private final File folder;
	private final MatrixData data;

	private MatrixDataExport(MatrixData data, File folder) {
		this.folder = folder;
		this.data = data;
	}

	static Library of(MatrixData data, File folder) {
		return new MatrixDataExport(data, folder).run();
	}

	private Library run() {
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				throw new RuntimeException(
						"Could not create folder: " + folder);
			}
		}
		writeInfo();
		writeMatrices();
		return new Library(folder);
	}

	private void writeInfo() {
		var fullName = folder.getName();
		var nameParts = fullName.split("_");
		var versionPart = nameParts.length == 1
				? null
				: nameParts[nameParts.length - 1];

		var name = fullName;
		var version = "";
		if (versionPart != null) {
			name = folder.getName().substring(0,
					fullName.length() - versionPart.length() + 1);
			version = Version.format(version);
		}

		var info = new LibraryInfo();
		info.name = name;
		info.version = version;
		info.isRegionalized = data.flowIndex != null
				&& data.flowIndex.isRegionalized;
		Json.write(info.toJson(), new File(folder, "library.json"));
	}

	private void writeMatrices() {
		writeMatrix("A", data.techMatrix);
		writeMatrix("B", data.flowMatrix);
		writeMatrix("C", data.impactMatrix);
		if (data.techMatrix == null)
			return;

		var solver = Julia.isLoaded()
				? new JuliaSolver()
				: new JavaSolver();

		// create the inverse
		var inv = solver.invert(data.techMatrix);
		writeMatrix("INV", inv);

		// create the intensity matrix
		if (data.flowMatrix == null)
			return;
		var m = solver.multiply(data.flowMatrix, inv);
		writeMatrix("M", m);
	}

	private void writeMatrix(String name, IMatrix matrix) {
		if (matrix == null)
			return;
		var m = matrix;
		if (m instanceof HashPointMatrix) {
			m = CSCMatrix.of(m);
		}
		if (m instanceof CSCMatrix) {
			var csc = (CSCMatrix) m;
			Npz.save(new File(folder, name + ".npz"), csc);
		} else {
			Npy.save(new File(folder, name + ".npy"), m);
		}
	}
}
