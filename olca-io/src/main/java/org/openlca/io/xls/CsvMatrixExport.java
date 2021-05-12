package org.openlca.io.xls;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;

import org.openlca.core.database.EntityCache;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.io.CategoryPair;
import org.openlca.io.DisplayValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes a product system as matrices into CSV files.
 */
@Deprecated
public class CsvMatrixExport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final EntityCache cache;
	private final CsvMatrixExportConfig conf;
	private final String separator;
	private final String point;
	private final HashMap<Long, String> categoryCache = new HashMap<>();

	public CsvMatrixExport(CsvMatrixExportConfig conf) {
		this.conf = conf;
		cache = conf.getEntityCache();
		separator = conf.columnSeperator;
		point = conf.decimalSeparator;
	}

	@Override
	public void run() {
		log.trace("Run matrix export");
		if (conf == null || !conf.valid()) {
			log.error("Invalid export data {}", conf);
			return;
		}

		log.trace("Build inventory matrix");
		CalculationSetup setup = new CalculationSetup(conf.productSystem);
		setup.parameterRedefs.addAll(conf.productSystem.parameterRedefs);
		setup.allocationMethod = AllocationMethod.NONE;
		var data = MatrixData.of(conf.db, setup);

		log.trace("Write technology matrix");
		try (FileWriter writer = new FileWriter(conf.technologyFile);
				BufferedWriter buffer = new BufferedWriter(writer)) {
			writeTechMatrix(data, buffer);
		} catch (Exception e) {
			log.error("Failed to write technology matrix", e);
		}

		log.trace("Write intervention matrix");
		try (FileWriter writer = new FileWriter(conf.interventionFile);
				BufferedWriter buffer = new BufferedWriter(writer)) {
			writeEnviMatrix(data, buffer);
		} catch (Exception e) {
			log.error("Failed to write intervention matrix", e);
		}

		log.trace("Export done");
	}

	private void writeTechMatrix(MatrixData data, BufferedWriter buffer)
			throws Exception {
		var techMatrix = data.techMatrix;
		TechIndex techIndex = data.techIndex;
		int size = techIndex.size();
		for (int row = 0; row < size; row++) {
			TechFlow product = techIndex.at(row);
			FlowDescriptor flow = product.flow();
			writeName(flow, buffer);
			sep(buffer);
			writeCategory(flow, buffer);
			sep(buffer);
			for (int col = 0; col < size; col++) {
				double val = techMatrix.get(row, col);
				writeValue(val, buffer);
				sep(buffer, col, size);
			}
			buffer.newLine();
		}
	}

	private void writeEnviMatrix(MatrixData data, BufferedWriter buffer)
			throws Exception {
		TechIndex techIndex = data.techIndex;
		EnviIndex flowIndex = data.enviIndex;
		int rows = flowIndex.size();
		int columns = techIndex.size();
		writeEnviMatrixHeader(buffer, techIndex);
		var matrix = data.enviMatrix;
		for (int row = 0; row < rows; row++) {
			FlowDescriptor flow = flowIndex.at(row).flow();
			writeName(flow, buffer);
			sep(buffer);
			writeCategory(flow, buffer);
			sep(buffer);
			for (int col = 0; col < columns; col++) {
				double val = matrix.get(row, col);
				writeValue(val, buffer);
				sep(buffer, col, columns);
			}
			buffer.newLine();
		}
	}

	private void writeEnviMatrixHeader(BufferedWriter buffer,
			TechIndex techIndex) throws Exception {
		sep(buffer);
		sep(buffer);
		int columns = techIndex.size();
		for (int col = 0; col < columns; col++) {
			TechFlow product = techIndex.at(col);
			FlowDescriptor flow = product.flow();
			writeName(flow, buffer);
			sep(buffer, col, columns);
		}
		buffer.newLine();
		sep(buffer);
		sep(buffer);
		for (int col = 0; col < columns; col++) {
			TechFlow product = techIndex.at(col);
			FlowDescriptor flow = product.flow();
			writeCategory(flow, buffer);
			sep(buffer, col, columns);
		}
		buffer.newLine();
	}

	private void writeName(FlowDescriptor flow, Writer buffer) {
		if (flow == null)
			return;
		String name = flow.name;
		try {
			String unit = DisplayValues.referenceUnit(flow, cache);
			name = name.concat(" [").concat(unit).concat("]");
			quote(name, buffer);
		} catch (Exception e) {
			log.error("Failed to load ref-unit", e);
		}
	}

	private void writeValue(double d, Writer buffer) throws Exception {
		String number = Double.toString(d);
		if (!point.equals(".")) {
			number = number.replace(".", point);
		}
		buffer.write(number);
	}

	private void quote(String val, Writer buffer) throws Exception {
		buffer.write('"');
		buffer.write(val);
		buffer.write('"');
	}

	private void sep(Writer buffer) throws Exception {
		buffer.append(separator);
	}

	private void sep(Writer buffer, int position, int dimension)
			throws Exception {
		if (position < dimension - 1)
			sep(buffer);
	}

	private void writeCategory(FlowDescriptor flow, Writer buffer)
			throws Exception {
		if (flow == null || flow.category == null)
			return;
		String catPath = categoryCache.get(flow.category);
		if (catPath == null) {
			catPath = CategoryPair.create(flow, cache).toPath();
			categoryCache.put(flow.category, catPath);
		}
		quote(catPath, buffer);
	}
}
