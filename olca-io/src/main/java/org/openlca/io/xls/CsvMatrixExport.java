package org.openlca.io.xls;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import org.openlca.core.database.EntityCache;
import org.openlca.core.math.DataStructures;
import org.openlca.core.matrix.ExchangeMatrix;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.io.CategoryPair;
import org.openlca.io.DisplayValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes a product system as matrices into CSV files.
 */
public class CsvMatrixExport implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private EntityCache cache;
	private CsvMatrixExportData data;
	private String separator;
	private String point;
	private HashMap<Long, String> categoryCache = new HashMap<>();

	public CsvMatrixExport(CsvMatrixExportData data) {
		this.data = data;
		cache = data.getEntityCache();
		separator = data.getColumnSeperator();
		point = data.getDecimalSeparator();
	}

	@Override
	public void run() {
		log.trace("Run matrix export");
		if (data == null || !data.valid()) {
			log.error("Invalid export data {}", data);
			return;
		}
		log.trace("Build inventory matrix");
		Inventory inventory = DataStructures.createInventory(
				data.getProductSystem(), data.getMatrixCache());
		log.trace("Write technology matrix");
		writeTechFile(inventory);
		log.trace("Write intervention matrix");
		writeEnviFile(inventory);
		log.trace("Export done");
	}

	private void writeTechFile(Inventory inventory) {
		try (FileWriter writer = new FileWriter(data.getTechnologyFile());
				BufferedWriter buffer = new BufferedWriter(writer)) {
			writeTechMatrix(inventory, buffer);
		} catch (Exception e) {
			log.error("Failed to write technology matrix", e);
		}
	}

	private void writeTechMatrix(Inventory inventory, BufferedWriter buffer)
			throws Exception {
		ExchangeMatrix techMatrix = inventory.technologyMatrix;
		TechIndex techIndex = inventory.productIndex;
		int size = techIndex.size();
		for (int row = 0; row < size; row++) {
			LongPair product = techIndex.getProviderAt(row);
			FlowDescriptor flow = getFlow(product.getSecond());
			writeName(flow, buffer);
			sep(buffer);
			writeCategory(flow, buffer);
			sep(buffer);
			for (int col = 0; col < size; col++) {
				double val = techMatrix.getValue(row, col);
				writeValue(val, buffer);
				sep(buffer, col, size);
			}
			buffer.newLine();
		}
	}

	private void writeEnviFile(Inventory inventory) {
		try (FileWriter writer = new FileWriter(data.getInterventionFile());
				BufferedWriter buffer = new BufferedWriter(writer)) {
			writeEnviMatrix(inventory, buffer);
		} catch (Exception e) {
			log.error("Failed to write intervention matrix", e);
		}
	}

	private void writeEnviMatrix(Inventory inventory, BufferedWriter buffer)
			throws Exception {
		TechIndex techIndex = inventory.productIndex;
		FlowIndex flowIndex = inventory.flowIndex;
		int rows = flowIndex.size();
		int columns = techIndex.size();
		writeEnviMatrixHeader(buffer, techIndex);
		ExchangeMatrix matrix = inventory.interventionMatrix;
		for (int row = 0; row < rows; row++) {
			FlowDescriptor flow = getFlow(flowIndex.getFlowAt(row));
			writeName(flow, buffer);
			sep(buffer);
			writeCategory(flow, buffer);
			sep(buffer);
			for (int col = 0; col < columns; col++) {
				double val = matrix.getValue(row, col);
				writeValue(val, buffer);
				sep(buffer, col, columns);
			}
			buffer.newLine();
		}

	}

	private void writeEnviMatrixHeader(BufferedWriter buffer,
			TechIndex techIndex) throws Exception, IOException {
		sep(buffer);
		sep(buffer);
		int columns = techIndex.size();
		for (int col = 0; col < columns; col++) {
			LongPair product = techIndex.getProviderAt(col);
			FlowDescriptor flow = getFlow(product.getSecond());
			writeName(flow, buffer);
			sep(buffer, col, columns);
		}
		buffer.newLine();
		sep(buffer);
		sep(buffer);
		for (int col = 0; col < columns; col++) {
			LongPair product = techIndex.getProviderAt(col);
			FlowDescriptor flow = getFlow(product.getSecond());
			writeCategory(flow, buffer);
			sep(buffer, col, columns);
		}
		buffer.newLine();
	}

	private FlowDescriptor getFlow(long id) {
		return cache.get(FlowDescriptor.class, id);
	}

	private void writeName(FlowDescriptor flow, Writer buffer) throws Exception {
		if (flow == null)
			return;
		String name = flow.getName();
		try {
			String unit = DisplayValues.referenceUnit(flow, cache);
			name = name.concat(" [").concat(unit).concat("]");
			quote(name, buffer);
		} catch (Exception e) {
			log.error("Failed to load ref-unit", e);
			return;
		}
	}

	private void writeValue(double d, Writer buffer) throws Exception {
		String number = Double.toString(d);
		if (!point.equals("."))
			number = number.replace(".", point);
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
		if (flow == null || flow.getCategory() == null)
			return;
		String catPath = categoryCache.get(flow.getCategory());
		if (catPath == null) {
			catPath = CategoryPair.create(flow, cache).toPath();
			categoryCache.put(flow.getCategory(), catPath);
		}
		quote(catPath, buffer);
	}

}
