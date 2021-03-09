package org.openlca.core.library;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.io.npy.Npy;
import org.openlca.core.matrix.io.npy.Npz;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;
import org.openlca.util.Exceptions;

class MatrixDataExport implements Runnable {

	private final File folder;
	private final MatrixData data;

	private MatrixDataExport(MatrixData data, File folder) {
		this.folder = folder;
		this.data = data;
	}

	@Override
	public void run() {
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				throw new RuntimeException(
						"Could not create folder: " + folder);
			}
		}
		writeIndices();
		writeMatrices();
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

		// we scale the tech. matrix to 1|-1 on the diag.
		var techMatrix = data.techMatrix;
		double[] scalings = null;
		if (techMatrix != null) {
			int n = techMatrix.rows();
			scalings = new double[n];
			boolean needScaling = false;
			for (int i = 0; i < n; i++) {
				var aii = Math.abs(techMatrix.get(i, i));
				if (aii == 1) {
					scalings[i] = 1;
				} else {
					scalings[i] = 1 / aii;
					needScaling = true;
				}
			}
			if (!needScaling) {
				scalings = null;
			} else {
				var copy = techMatrix.asMutableCopy();
				copy.scaleColumns(scalings);
				techMatrix = copy;
			}
			writeMatrix("A", techMatrix);
		}

		var flowMatrix = data.flowMatrix;
		if (flowMatrix != null) {
			if (scalings != null) {
				var copy = flowMatrix.asMutableCopy();
				copy.scaleColumns(scalings);
				flowMatrix = copy;
			}
			writeMatrix("B", flowMatrix);
		}

		if (data.impactMatrix != null) {
			writeMatrix("C", data.impactMatrix);
		}

		if (techMatrix == null)
			return;

		var solver = Julia.isLoaded()
				? new JuliaSolver()
				: new JavaSolver();

		// create the inverse
		var inv = solver.invert(techMatrix);
		writeMatrix("INV", inv);

		// create the intensity matrix
		if (flowMatrix == null)
			return;
		var m = solver.multiply(flowMatrix, inv);
		writeMatrix("M", m);
	}

	private void writeMatrix(String name, MatrixReader matrix) {
		MatrixReader m = matrix;
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
