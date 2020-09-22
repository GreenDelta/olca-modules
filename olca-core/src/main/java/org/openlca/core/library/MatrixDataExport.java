package org.openlca.core.library;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

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
import org.openlca.util.Exceptions;

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
		writeIndices();
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
			name = fullName.substring(0,
					fullName.length() - versionPart.length() - 1);
			version = Version.format(versionPart);
		}

		var info = new LibraryInfo();
		info.name = name;
		info.version = version;
		info.isRegionalized = data.flowIndex != null
				&& data.flowIndex.isRegionalized;
		Json.write(info.toJson(), new File(folder, "library.json"));
	}

	private void writeIndices() {

		if (data.techIndex != null) {
			var products = Proto.ProductIndex.newBuilder();
			data.techIndex.each((index, product) -> {
				var entry = LibIndex.protoEntry(index, product);
				products.addProduct(entry);
			});
			write("index_A.bin", out -> products.build().writeTo(out));
		}

		if (data.flowIndex != null) {
			var flows = Proto.ElemFlowIndex.newBuilder();
			data.flowIndex.each((index, iFlow) -> {
				var entry = LibIndex.protoEntry(index, iFlow);
				flows.addFlow(entry);
			});
			write("index_B.bin", out -> flows.build().writeTo(out));
		}

		if (data.impactIndex != null) {
			var impacts = Proto.ImpactIndex.newBuilder();
			data.impactIndex.each((index, impact) -> {
				var entry = LibIndex.protoEntry(index, impact);
				impacts.addImpact(entry);
			});
			write("index_C.bin", out -> impacts.build().writeTo(out));
		}
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

	private void write(String file, IndexWriter.Output fn) {
		var f = new File(folder, file);
		try (var stream = new FileOutputStream(f);
			 var buffer = new BufferedOutputStream(stream)) {
			fn.accept(buffer);
		} catch (Exception e) {
			Exceptions.unchecked("failed to write file " + f, e);
		}
	}
}
