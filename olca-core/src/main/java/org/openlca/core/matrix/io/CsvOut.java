package org.openlca.core.matrix.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.function.Consumer;

import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * This class provides methods for writing matrices and related indices to CSV
 * files. It has the following properties:
 *
 * <ul>
 * <li>existing files are overwritten
 * <li>the column separator is a comma: `,`
 * <li>the file encoding is `UTF-8`
 * <li>the line separator is platform specific
 * <li>internal exceptions are rethrown as runtime exceptions, handling them is
 * up to you
 * </ul>
 */
public final class CsvOut {

	private CsvOut() {
	}

	// TODO: not yet sure if we need the entity cache here

	/**
	 * Write the matrix to the given file.
	 */
	public static void write(IMatrix matrix, File file) {
		if (matrix == null || file == null)
			return;
		String[] mask = new String[matrix.columns()];
		writer(file, w -> {
			for (int row = 0; row < matrix.rows(); row++) {
				for (int col = 0; col < matrix.columns(); col++) {
					mask[col] = Double.toString(matrix.get(row, col));
				}
				writeln(w, line(mask));
			}
		});
	}

	/**
	 * Write the array as column vector to the given file.
	 */
	public static void writeCol(double[] v, File file) {
		if (v == null || file == null)
			return;
		writer(file, w -> {
			for (int i = 0; i < v.length; i++) {
				writeln(w, Double.toString(v[i]));
			}
		});
	}

	/**
	 * Write the product index into the given file.
	 */
	public static void write(TechIndex idx, File file) {
		if (idx == null || file == null)
			return;

		String[] header = {
				"index",
				"process ID",
				"process name",
				"process type",
				"process location",
				"process category",
				"flow ID",
				"flow name",
				"flow type",
				"flow location",
				"flow category",
				"flow unit" };

		writer(file, w -> {
			writeln(w, line(header));
			String[] mask = new String[header.length];
			for (int i = 0; i < idx.size(); i++) {
				ProcessProduct product = idx.getProviderAt(i);
				CategorizedDescriptor p = product.process;
				FlowDescriptor f = product.flow;
				mask[0] = Integer.toString(i);
				mask[1] = p.getRefId();
				mask[2] = p.getName();
				if (p instanceof ProcessDescriptor) {
					ProcessType t = ((ProcessDescriptor) p).getProcessType();
					mask[3] = t != null
							? t.toString()
							: ModelType.PROCESS.toString();
				} else {
					mask[3] = ModelType.PRODUCT_SYSTEM.toString();
				}
				mask[4] = ""; // TODO: process location
				mask[5] = ""; // TODO: process category
				mask[6] = f.getRefId();
				mask[7] = f.getName();
				mask[8] = f.getFlowType() != null
						? f.getFlowType().toString()
						: "";
				mask[9] = ""; // TODO: flow location
				mask[10] = ""; // TODO: flow category
				mask[11] = ""; // TODO: flow unit
				writeln(w, line(mask));
			}
		});
	}

	/**
	 * Write the flow index into the given file.
	 */
	public static void write(FlowIndex idx, File file) {
		if (idx == null || file == null)
			return;

		String[] header = {
				"index",
				"flow ID",
				"flow name",
				"flow type",
				"flow location",
				"flow category",
				"flow unit" };

		writer(file, w -> {
			writeln(w, line(header));
			String[] mask = new String[header.length];
			for (int i = 0; i < idx.size(); i++) {
				FlowDescriptor flow = idx.at(i);
				mask[0] = Integer.toString(i);
				mask[1] = flow.getRefId();
				mask[2] = flow.getName();
				mask[3] = flow.getFlowType() != null
						? flow.getFlowType().toString()
						: "";
				mask[4] = ""; // TODO location code
				mask[5] = ""; // TODO category path
				mask[6] = ""; // TODO unit
				writeln(w, line(mask));
			}
		});
	}

	/**
	 * Write the LCIA category index into the given file.
	 */
	public static void write(DIndex<ImpactCategoryDescriptor> idx, File file) {
		String[] header = {
				"index",
				"impact ID",
				"impact name",
				"impact ref. unit" };

		writer(file, w -> {
			writeln(w, line(header));
			String[] mask = new String[header.length];
			for (int i = 0; i < idx.size(); i++) {
				ImpactCategoryDescriptor d = idx.at(i);
				mask[0] = Integer.toString(i);
				mask[1] = d.getRefId();
				mask[2] = d.getName();
				mask[3] = d.getReferenceUnit();
				writeln(w, line(mask));
			}
		});
	}

	private static String line(String[] entries) {
		if (entries == null)
			return "";
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < entries.length; i++) {
			String e = entries[i];
			boolean last = i == entries.length - 1;
			if (e == null) {
				if (!last)
					b.append(',');
				continue;
			}
			e = e.trim().replace('"', '\'');
			if (e.indexOf(',') >= 0) {
				b.append('"').append(e).append('"');
			} else {
				b.append(e);
			}
			if (!last)
				b.append(',');
		}
		return b.toString();
	}

	private static void writer(File file, Consumer<BufferedWriter> fn) {
		try (FileOutputStream stream = new FileOutputStream(file);
				Writer writer = new OutputStreamWriter(stream, "utf-8");
				BufferedWriter buffer = new BufferedWriter(writer)) {
			fn.accept(buffer);
			buffer.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void writeln(BufferedWriter writer, String line) {
		try {
			writer.write(line);
			writer.newLine();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
