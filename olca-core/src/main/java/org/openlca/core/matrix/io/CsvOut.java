package org.openlca.core.matrix.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.BaseResult;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.util.CategoryPathBuilder;

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

	/**
	 * Write the given matrix data and indices as CSV files to the given folder.
	 */
	public static void write(MatrixData data, IDatabase db, File folder) {
		if (data == null || folder == null)
			return;
		if (!folder.exists()) {
			folder.mkdirs();
		}
		write(data.techIndex, db, new File(folder, "indexA.csv"));
		write(data.flowIndex, db, new File(folder, "indexB.csv"));
		write(data.impactIndex, new File(folder, "indexC.csv"));

		write(data.techMatrix, new File(folder, "A.csv"));
		write(data.enviMatrix, new File(folder, "B.csv"));
		write(data.impactMatrix, new File(folder, "C.csv"));
		writeCol(data.costVector, new File(folder, "k.csv"));
	}

	/**
	 * Write the result to the given folder.
	 */
	public static void write(BaseResult result, IDatabase db, File folder) {
		if (result == null || folder == null)
			return;
		if (!folder.exists()) {
			folder.mkdirs();
		}
		write(result.techIndex, db, new File(folder, "indexA.csv"));
		write(result.flowIndex, db, new File(folder, "indexB.csv"));
		write(result.impactIndex, new File(folder, "indexC.csv"));

		if (result instanceof SimpleResult) {
			SimpleResult sr = (SimpleResult) result;
			writeCol(sr.scalingVector, new File(folder, "s.csv"));
			writeCol(sr.totalRequirements, new File(folder, "t.csv"));
			writeCol(sr.totalFlowResults, new File(folder, "g.csv"));
			writeCol(sr.totalImpactResults, new File(folder, "h.csv"));
		}

		if (result instanceof ContributionResult) {
			ContributionResult cr = (ContributionResult) result;
			write(cr.directFlowResults, new File(folder, "G.csv"));
			write(cr.directImpactResults, new File(folder, "H.csv"));
			writeCol(cr.directCostResults, new File(folder, "k_scaled.csv"));
			write(cr.impactFactors, new File(folder, "C.csv"));
		}

		if (result instanceof FullResult) {
			FullResult fr = (FullResult) result;
			write(fr.techMatrix, new File(folder, "A_scaled.csv"));
			write(fr.upstreamFlowResults, new File(folder, "U.csv"));
			write(fr.upstreamImpactResults, new File(folder, "V.csv"));
			write(fr.upstreamCostResults, new File(folder, "k_upstreams.csv"));
		}
	}

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
	public static void write(TechIndex idx, IDatabase db, File file) {
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

		CategoryPathBuilder categories = new CategoryPathBuilder(db);
		Map<Long, String> locations = new LocationDao(db).getCodes();
		Map<Long, String> units = propUnits(db);

		writer(file, w -> {
			writeln(w, line(header));
			String[] mask = new String[header.length];
			for (int i = 0; i < idx.size(); i++) {
				ProcessProduct product = idx.getProviderAt(i);
				CategorizedDescriptor p = product.process;
				FlowDescriptor f = product.flow;
				mask[0] = Integer.toString(i);
				mask[1] = p.refId;
				mask[2] = p.name;
				if (p instanceof ProcessDescriptor) {
					ProcessDescriptor pd = (ProcessDescriptor) p;
					mask[3] = pd.processType != null
							? pd.processType.toString()
							: ModelType.PROCESS.toString();
					mask[4] = locations.get(pd.location);
				} else {
					mask[3] = ModelType.PRODUCT_SYSTEM.toString();
					mask[4] = "";
				}
				mask[5] = categories.build(p.category);
				mask[6] = f.refId;
				mask[7] = f.name;
				mask[8] = f.flowType != null
						? f.flowType.toString()
						: "";
				mask[9] = locations.get(f.location);
				mask[10] = categories.build(f.category);
				mask[11] = units.get(f.refFlowPropertyId);
				writeln(w, line(mask));
			}
		});
	}

	/**
	 * Write the flow index into the given file.
	 */
	public static void write(FlowIndex idx, IDatabase db, File file) {
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

		CategoryPathBuilder categories = new CategoryPathBuilder(db);
		Map<Long, String> locations = new LocationDao(db).getCodes();
		Map<Long, String> units = propUnits(db);

		writer(file, w -> {
			writeln(w, line(header));
			String[] mask = new String[header.length];
			for (int i = 0; i < idx.size(); i++) {
				FlowDescriptor flow = idx.at(i).flow;
				mask[0] = Integer.toString(i);
				mask[1] = flow.refId;
				mask[2] = flow.name;
				mask[3] = flow.flowType != null
						? flow.flowType.toString()
						: "";
				mask[4] = locations.get(flow.location);
				mask[5] = categories.build(flow.category);
				mask[6] = units.get(flow.refFlowPropertyId);
				writeln(w, line(mask));
			}
		});
	}

	/**
	 * Write the LCIA category index into the given file.
	 */
	public static void write(DIndex<ImpactCategoryDescriptor> idx, File file) {
		if (idx == null || file == null)
			return;

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
				mask[1] = d.refId;
				mask[2] = d.name;
				mask[3] = d.referenceUnit;
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

	/**
	 * Returns a map `flow property ID -> reference unit name` for the flow
	 * properties in the database.
	 */
	private static Map<Long, String> propUnits(IDatabase db) {
		try {
			String sql = "select fp.id, u.name from tbl_flow_properties as fp"
					+ "  inner join tbl_unit_groups ug"
					+ "  on fp.f_unit_group = ug.id"
					+ "  inner join tbl_units u"
					+ "  on ug.f_reference_unit = u.id";
			HashMap<Long, String> m = new HashMap<>();
			NativeSql.on(db).query(sql, r -> {
				m.put(r.getLong(1), r.getString(2));
				return true;
			});
			return m;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
