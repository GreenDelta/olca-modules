package org.openlca.io.xls.results;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.openlca.core.database.EntityCache;
import org.openlca.core.matrices.FlowIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.SimulationResult;
import org.openlca.core.results.SimulationResults;
import org.openlca.io.xls.Excel;

/** Exports a simulation result to Excel. */
public class SimulationResultExport {

	private SimulationResult result;
	private EntityCache cache;
	private int row = 0;
	private CellStyle headerStyle;

	public SimulationResultExport(SimulationResult result, EntityCache cache) {
		this.result = result;
		this.cache = cache;
	}

	public void run(File file) throws Exception {
		HSSFWorkbook workbook = new HSSFWorkbook();
		headerStyle = Excel.headerStyle(workbook);
		writeInventorySheet(workbook);
		if (result.hasImpactResults())
			writeImpactSheet(workbook);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			workbook.write(fos);
		}
	}

	private void writeImpactSheet(HSSFWorkbook workbook) {
		HSSFSheet sheet = workbook.createSheet("Impact Assessment");
		row = 0;
		writeSheetHeader(sheet);
		writerImpactHeader(sheet);
		List<SimulationExportImpact> buckets = createCategoryBuckets();
		for (SimulationExportImpact bucket : buckets) {
			HSSFRow aRow = sheet.createRow(row++);
			bucket.writeRow(aRow, result);
		}
		for (int i = 0; i < 9; i++)
			sheet.autoSizeColumn(i);
	}

	private List<SimulationExportImpact> createCategoryBuckets() {
		List<SimulationExportImpact> buckets = new ArrayList<>();
		for (ImpactCategoryDescriptor category : SimulationResults.getImpacts(
				result, cache)) {
			SimulationExportImpact bucket = new SimulationExportImpact(category);
			buckets.add(bucket);
		}
		Collections.sort(buckets);
		return buckets;
	}

	private void writerImpactHeader(HSSFSheet sheet) {
		row++;
		HSSFRow headerRow = sheet.createRow(row++);
		String[] headers = SimulationExportImpact.getHeaders();
		for (int i = 0; i < headers.length; i++)
			headerCell(headerRow, i, headers[i]);
	}

	private void writeInventorySheet(HSSFWorkbook workbook) {
		HSSFSheet sheet = workbook.createSheet("Inventory");
		row = 0;
		writeSheetHeader(sheet);
		FlowIndex flowIndex = result.getFlowIndex();
		List<SimulationExportFlow> flowBuckets = createFlowBuckets(flowIndex);
		writeInventorySection(flowBuckets, true, sheet);
		writeInventorySection(flowBuckets, false, sheet);
		for (int i = 0; i < 11; i++)
			sheet.autoSizeColumn(i);
	}

	private List<SimulationExportFlow> createFlowBuckets(FlowIndex index) {
		List<SimulationExportFlow> buckets = new ArrayList<>();
		for (FlowDescriptor flow : SimulationResults.getFlows(result, cache)) {
			boolean input = index.isInput(flow.getId());
			SimulationExportFlow bucket = new SimulationExportFlow(flow, input,
					cache);
			buckets.add(bucket);
		}
		Collections.sort(buckets);
		return buckets;
	}

	private void writeInventorySection(List<SimulationExportFlow> flowBuckets,
			boolean forInputs, HSSFSheet sheet) {
		String header = forInputs ? "Inputs" : "Outputs";
		writeInventoryHeader(header, sheet);
		for (SimulationExportFlow bucket : flowBuckets) {
			if (bucket.isInput() == forInputs) {
				HSSFRow aRow = sheet.createRow(row++);
				bucket.writeRow(aRow, result);
			}
		}
	}

	private void writeSheetHeader(HSSFSheet sheet) {
		// TODO: we need a Simulation setup here
		// String[] labels = { "Product system", "Process",
		// "Quantitative reference" };
		// String[] values = { input.getName(), input.getReferenceProcessName(),
		// input.getQuantitativeReference() };
		// for (int i = 0; i < labels.length; i++) {
		// HSSFRow aRow = sheet.createRow(row++);
		// headerCell(aRow, 0, labels[i]);
		// Excel.cell(aRow, 1, values[i]);
		// }
		// HSSFRow aRow = sheet.createRow(row++);
		// headerCell(aRow, 0, "Number of simulations");
		// aRow.createCell(1).setCellValue(input.getNumberOfRuns());
	}

	private void writeInventoryHeader(String section, HSSFSheet sheet) {
		row++;
		HSSFRow aRow = sheet.createRow(row++);
		headerCell(aRow, 0, section);
		aRow = sheet.createRow(row++);
		String[] columns = SimulationExportFlow.getHeaders();
		for (int i = 0; i < columns.length; i++)
			headerCell(aRow, i, columns[i]);
	}

	private Cell headerCell(HSSFRow row, int column, String label) {
		Cell cell = Excel.cell(row, column, label);
		cell.setCellStyle(headerStyle);
		return cell;
	}

}
