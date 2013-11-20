package org.openlca.io.xls.results;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.SimulationResult;
import org.openlca.core.results.SimulationStatistics;
import org.openlca.io.xls.Excel;

/** Exports a simulation result to Excel. */
public class SimulationResultExport {

	private SimulationResult result;
	private EntityCache cache;
	private int row = 0;
	private CellWriter writer;

	public SimulationResultExport(SimulationResult result, EntityCache cache) {
		this.result = result;
		this.cache = cache;
	}

	/**
	 * Runs the result export. The given file should be an xlsx file.
	 */
	public void run(File file) throws Exception {
		Workbook workbook = new XSSFWorkbook();
		writer = new CellWriter(cache, workbook);
		writeInventorySheet(workbook);
		if (result.hasImpactResults())
			writeImpactSheet(workbook);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			workbook.write(fos);
		}
	}

	private void writeImpactSheet(Workbook workbook) {
		Sheet sheet = workbook.createSheet("Impact Assessment");
		row = 0;
		writerImpactHeader(sheet);
		List<ImpactCategoryDescriptor> impacts = Utils.getSortedImpacts(
				result.getImpactIndex(), cache);
		for (ImpactCategoryDescriptor impact : impacts) {
			writer.writeImpactRowInfo(sheet, row, impact);
			List<Double> values = result.getImpactResults(impact.getId());
			writeValues(sheet, row, CellWriter.IMPACT_INFO_SIZE + 1, values);
			row++;
		}
		for (int i = 0; i < CellWriter.IMPACT_INFO_SIZE + 7; i++)
			sheet.autoSizeColumn(i);
	}

	private void writeInventorySheet(Workbook workbook) {
		Sheet sheet = workbook.createSheet("Inventory");
		row = 0;
		FlowIndex flowIndex = result.getFlowIndex();
		List<FlowDescriptor> flows = Utils.getSortedFlows(flowIndex, cache);
		writeInventorySection(flows, true, sheet);
		writeInventorySection(flows, false, sheet);
		for (int i = 0; i < CellWriter.FLOW_INFO_SIZE + 7; i++)
			sheet.autoSizeColumn(i);
	}

	private void writeInventorySection(List<FlowDescriptor> flows,
			boolean forInputs, Sheet sheet) {
		writeInventoryHeader(sheet, forInputs);
		FlowIndex idx = result.getFlowIndex();
		for (FlowDescriptor flow : flows) {
			if (idx.isInput(flow.getId()) != forInputs)
				continue;
			writer.writeFlowRowInfo(sheet, row, flow);
			List<Double> values = result.getFlowResults(flow.getId());
			writeValues(sheet, row, CellWriter.FLOW_INFO_SIZE + 1, values);
			row++;
		}
	}

	private void writeSheetHeader(Sheet sheet) {
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

	private void writeInventoryHeader(Sheet sheet, boolean inputs) {
		row++;
		String section = inputs ? "Inputs" : "Outputs";
		writer.header(sheet, row, 1, section);
		row++;
		writer.writeFlowRowHeader(sheet, row);
		int nextCol = CellWriter.FLOW_INFO_SIZE + 1;
		writeValueHeaders(sheet, row, nextCol);
		row++;
	}

	private void writerImpactHeader(Sheet sheet) {
		row++;
		writer.writeImpactRowHeader(sheet, row);
		int nextCol = CellWriter.IMPACT_INFO_SIZE + 1;
		writeValueHeaders(sheet, row, nextCol);
		row++;
	}

	private void writeValueHeaders(Sheet sheet, int row, int startCol) {
		String[] vals = { "Mean", "Standard deviation", "Minimum", "Maximum",
				"Median", "5% Percentile", "95% Percentile" };
		for (int i = 0; i < vals.length; i++)
			writer.header(sheet, row, startCol + i, vals[i]);
		int nextCol = startCol + vals.length;
		for (int i = 0; i < result.getNumberOfRuns(); i++)
			writer.header(sheet, row, nextCol++, "Run " + (i + 1));
	}

	private void writeValues(Sheet sheet, int row, int startCol,
			List<Double> values) {
		if (values == null)
			return;
		int col = startCol;
		SimulationStatistics stat = new SimulationStatistics(values, 100);
		Excel.cell(sheet, row, col++, stat.getMean());
		Excel.cell(sheet, row, col++, stat.getStandardDeviation());
		Excel.cell(sheet, row, col++, stat.getMinimum());
		Excel.cell(sheet, row, col++, stat.getMaximum());
		Excel.cell(sheet, row, col++, stat.getMedian());
		Excel.cell(sheet, row, col++, stat.getPercentileValue(5));
		Excel.cell(sheet, row, col++, stat.getPercentileValue(95));
		for (int i = 0; i < values.size(); i++)
			Excel.cell(sheet, row, col++, values.get(i).doubleValue());
	}

}
