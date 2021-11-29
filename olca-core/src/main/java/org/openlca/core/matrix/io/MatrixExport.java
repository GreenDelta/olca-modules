package org.openlca.core.matrix.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.ByteMatrixBuffer;
import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.uncertainties.UMatrix;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UncertaintyType;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.Categories;

public abstract class MatrixExport {

	protected final IDatabase db;
	protected final File folder;
	protected final MatrixData data;

	protected MatrixExport(IDatabase db, File folder, MatrixData data) {
		this.db = db;
		this.data = data;
		this.folder = folder;
		if (!folder.exists()) {
			try {
				Files.createDirectories(folder.toPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static MatrixExport toNpy(IDatabase db, File folder, MatrixData data) {
		return new NpyExport(db, folder, data);
	}

	public static void toNpy(File folder, MatrixReader matrix, String name) {
		if (folder == null || matrix == null)
			return;
		if (!folder.exists()) {
			try {
				Files.createDirectories(folder.toPath());
			} catch (IOException e) {
				throw new RuntimeException("failed to create folder " + folder, e);
			}
		}
		NpyMatrix.write(folder, name, matrix);
	}

	public static CsvExport toCsv(IDatabase db, File folder, MatrixData data) {
		return new CsvExport(db, folder, data);
	}

	/**
	 * Writes the indices and matrices into the respective export format.
	 */
	public void writeAll() {
		writeIndices();
		writeMatrices();
	}

	/**
	 * Writes only the matrices without the indices into the respective format.
	 */
	public void writeMatrices() {

		if (data.techMatrix != null) {
			write(data.techMatrix, "A");
		}
		if (data.enviMatrix != null) {
			write(data.enviMatrix, "B");
		}
		if (data.impactMatrix != null) {
			write(data.impactMatrix, "C");
		}
		if (data.costVector != null) {
			write(data.costVector, "costs");
		}

		// write the demand vector in case of a linked tech. index
		var techIndex = data.techIndex;
		if (techIndex != null && techIndex.hasLinks()) {
			var demand = new double[techIndex.size()];
			int refIdx = techIndex.of(techIndex.getRefFlow());
			if (refIdx >= 0) {
				demand[refIdx] = techIndex.getDemand();
			}
			write(demand, "f");
		}

		uncertainties(data.techMatrix, data.techUncertainties, "A");
		uncertainties(data.enviMatrix, data.enviUncertainties, "B");
		uncertainties(data.impactMatrix, data.impactUncertainties, "C");
	}

	private void uncertainties(MatrixReader host, UMatrix u, String prefix) {
		if (host == null || u == null)
			return;

		var types = new ByteMatrixBuffer();
		var builders = new MatrixBuilder[3];

		u.each((row, col, cell) -> {

			var type = cell.type();
			if (type == null || type == UncertaintyType.NONE)
				return;
			var values = cell.values();
			if (values == null || values.length == 0)
				return;

			types.set(row, col, UncertaintyType.byteIndexOf(type));
			for (int i = 0; i < values.length; i++) {
				if (i > 2)
					break;
				var builder = builders[i];
				if (builder == null) {
					builder = new MatrixBuilder();
					builders[i] = builder;
				}
				builder.set(row, col, values[i]);
			}
		});

		if (types.isEmpty())
			return;

		// write the type matrix
		types.minSize(host.rows(), host.columns());
		var typeMatrix = types.finish();
		write(typeMatrix, prefix + "_utype");

		// write the value matrices
		for (int i = 0; i < builders.length; i++) {
			var builder = builders[i];
			if (builder == null || builder.isEmpty())
				continue;
			builder.minSize(host.rows(), host.columns());
			var matrix = builder.finish();
			write(matrix, prefix + "_u" + i);
		}
	}

	protected abstract void write(MatrixReader matrix, String name);

	protected abstract void write(double[] vector, String name);

	protected abstract void write(ByteMatrixReader matrix, String name);

	/**
	 * Writes only the indices of the matrix data into the respective format.
	 */
	public abstract void writeIndices();

	protected void eachTechIndexRow(Consumer<String[]> fn) {
		if (data.techIndex == null || fn == null)
			return;

		String[] header = {
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
			"flow unit",
		};
		fn.accept(header);

		var categories = Categories.pathsOf(db);
		var locations = new LocationDao(db).getCodes();
		var units = propUnits();
		for (int i = 0; i < data.techIndex.size(); i++) {
			var product = data.techIndex.at(i);
			var p = product.provider();
			var f = product.flow();
			var row = new String[header.length];
			row[0] = p.refId;
			row[1] = p.name;
			if (p instanceof ProcessDescriptor) {
				var pd = (ProcessDescriptor) p;
				row[2] = pd.processType != null
					? pd.processType.toString()
					: ModelType.PROCESS.toString();
				row[3] = locations.get(pd.location);
			} else {
				row[2] = ModelType.PRODUCT_SYSTEM.toString();
				row[3] = "";
			}
			row[4] = categories.pathOf(p.category);
			row[5] = f.refId;
			row[6] = f.name;
			row[7] = f.flowType != null
				? f.flowType.toString()
				: "";
			row[8] = locations.get(f.location);
			row[9] = categories.pathOf(f.category);
			row[10] = units.get(f.refFlowPropertyId);

			for (int j = 0; j < row.length; j++) {
				if(row[j] == null) {
					row[j] = "";
				}
			}
			fn.accept(row);
		}
	}

	protected void eachFlowIndexRow(Consumer<String[]> fn) {
		if (data.enviIndex == null || fn == null)
			return;
		String[] header = {
			"flow ID",
			"flow name",
			"flow type",
			"flow category",
			"flow unit",
			"location"};
		fn.accept(header);

		var categories = Categories.pathsOf(db);
		var units = propUnits();
		for (int i = 0; i < data.enviIndex.size(); i++) {
			var  iFlow = data.enviIndex.at(i);
			var row = new String[header.length];
			if (iFlow == null) {
				fn.accept(row);
				continue;
			}
			var flow = iFlow.flow();
			row[0] = flow.refId;
			row[1] = flow.name;
			row[2] = flow.flowType != null
				? flow.flowType.toString()
				: "";
			row[3] = categories.pathOf(flow.category);
			row[4] = units.get(flow.refFlowPropertyId);
			if (iFlow.location() != null) {
				row[5] = iFlow.location().code;
			}

			for (int j = 0; j < row.length; j++) {
				if(row[j] == null) {
					row[j] = "";
				}
			}
			fn.accept(row);
		}
	}

	protected void eachImpactIndexRow(Consumer<String[]> fn) {
		if (data.impactIndex == null || fn == null)
			return;
		String[] header = {
			"impact ID",
			"impact name",
			"impact ref. unit" };
		fn.accept(header);

		for (int i = 0; i < data.impactIndex.size(); i++) {
			var impact = data.impactIndex.at(i);
			var row = new String[header.length];
			row[0] = impact.refId;
			row[1] = impact.name;
			row[2] = impact.referenceUnit;

			for (int j = 0; j < row.length; j++) {
				if(row[j] == null) {
					row[j] = "";
				}
			}
			fn.accept(row);
		}
	}

	/**
	 * Returns a map `flow property ID -> reference unit name` for the flow
	 * properties in the database.
	 */
	private Map<Long, String> propUnits() {
		try {
			String sql = "select fp.id, u.name from tbl_flow_properties as fp"
									 + "  inner join tbl_unit_groups ug"
									 + "  on fp.f_unit_group = ug.id"
									 + "  inner join tbl_units u"
									 + "  on ug.f_reference_unit = u.id";
			var m = new HashMap<Long, String>();
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
