package org.openlca.core.matrix.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ImpactIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.BaseResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.util.CategoryPathBuilder;

public final class Csv {

	private final DecimalFormat numberFormat;
	private String delimiter = ",";
	private Charset charset = StandardCharsets.UTF_8;

	public Csv() {
		numberFormat = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		numberFormat.setMaximumFractionDigits(1000);
	}

	public static Csv defaultConfig() {
		return new Csv();
	}

	public Csv withDelimiter(String delimiter) {
		if (delimiter != null) {
			this.delimiter = delimiter;
		}
		return this;
	}

	public Csv withEncoding(Charset charset) {
		if (charset != null) {
			this.charset = charset;
		}
		return this;
	}

	public Csv withDecimalSeparator(char separator) {
		var symbols = new DecimalFormat().getDecimalFormatSymbols();
		symbols.setDecimalSeparator(separator);
		symbols.setGroupingSeparator(separator == ',' ? '.' : ',');
		numberFormat.setDecimalFormatSymbols(symbols);
		return this;
	}

	public void write(double[] vector, File file) {
		if (vector == null || file == null)
			return;
		writer(file, w -> {
			for (double v : vector) {
				writeln(w, numberFormat.format(v));
			}
		});
	}

	public void write(MatrixReader matrix, File file) {
		if (matrix == null || file == null)
			return;
		var buffer = new String[matrix.columns()];
		writer(file, w -> {
			for (int row = 0; row < matrix.rows(); row++) {
				for (int col = 0; col < matrix.columns(); col++) {
					buffer[col] = numberFormat.format(matrix.get(row, col));
				}
				writeln(w, line(buffer));
			}
		});
	}

	public void write(ByteMatrixReader matrix, File file) {
		if (matrix == null || file == null)
			return;
		var buffer = new String[matrix.columns()];
		writer(file, w -> {
			for (int row = 0; row < matrix.rows(); row++) {
				for (int col = 0; col < matrix.columns(); col++) {
					buffer[col] = Integer.toString(matrix.get(row, col));
				}
				writeln(w, line(buffer));
			}
		});
	}

	void writeln(BufferedWriter writer, String line) {
		try {
			writer.write(line);
			writer.newLine();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	void writer(File file, Consumer<BufferedWriter> fn) {
		try (var stream = new FileOutputStream(file);
				 var writer = new OutputStreamWriter(stream, charset);
				 var buffer = new BufferedWriter(writer)) {
			fn.accept(buffer);
			buffer.flush();
		} catch (IOException e) {
			throw new RuntimeException("Failed to write file: " + file.getName(), e);
		}
	}

	String line(String[] entries) {
		if (entries == null)
			return "";
		var b = new StringBuilder();
		for (int i = 0; i < entries.length; i++) {
			if (i != 0) {
				b.append(delimiter);
			}
			var e = entries[i];
			if(e != null) {
				b.append(e);
			}
		}
		return b.toString();
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
				mask[5] = categories.path(p.category);
				mask[6] = f.refId;
				mask[7] = f.name;
				mask[8] = f.flowType != null
						? f.flowType.toString()
						: "";
				mask[9] = locations.get(f.location);
				mask[10] = categories.path(f.category);
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
				mask[5] = categories.path(flow.category);
				mask[6] = units.get(flow.refFlowPropertyId);
				writeln(w, line(mask));
			}
		});
	}

	/**
	 * Write the LCIA category index into the given file.
	 */
	public static void write(ImpactIndex idx, File file) {
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
				ImpactDescriptor d = idx.at(i);
				mask[0] = Integer.toString(i);
				mask[1] = d.refId;
				mask[2] = d.name;
				mask[3] = d.referenceUnit;
				writeln(w, line(mask));
			}
		});
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
