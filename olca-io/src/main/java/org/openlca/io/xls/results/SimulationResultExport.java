package org.openlca.io.xls.results;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.SimulationResultProvider;
import org.openlca.core.results.SimulationStatistics;
import org.openlca.io.xls.Excel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Exports a simulation result to Excel. */
public class SimulationResultExport {

	private static final String[] FLOW_HEADER = { "Flow UUID", "Flow", "Category", "Sub-category", "Unit" };
	private static final String[] IMPACT_HEADER = { "Impact category UUID", "Impact category", "Reference unit" };
	private Logger log = LoggerFactory.getLogger(getClass());

	private CalculationSetup setup;
	private SimulationResultProvider<?> result;
	private EntityCache cache;
	private int row = 0;
	private CellWriter writer;
	private boolean useStreaming = false;

	public SimulationResultExport(CalculationSetup setup,
			SimulationResultProvider<?> result) {
		this.setup = setup;
		this.result = result;
		this.cache = result.cache;
	}

	/**
	 * Runs the result export. The given file should be an xlsx file.
	 */
	public void run(File file) throws Exception {
		Workbook workbook = createWorkbook();
		writer = new CellWriter(cache, workbook);
		InfoSheet.write(workbook, writer, setup, null, "Simulation result");
		writeInventorySheet(workbook);
		if (result.hasImpactResults())
			writeImpactSheet(workbook);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			workbook.write(fos);
		}
		log.trace("result written to file {}", file);
	}

	private Workbook createWorkbook() {
		useStreaming = result.getNumberOfRuns() > 150;
		log.trace("create workbook, using streaming: {}", useStreaming);
		if (useStreaming)
			return new SXSSFWorkbook(-1);
		else
			return new XSSFWorkbook();
	}

	private void writeImpactSheet(Workbook workbook) {
		Sheet sheet = workbook.createSheet("Impact Assessment");
		row = 0;
		writerImpactHeader(sheet);
		List<ImpactCategoryDescriptor> impacts = Sort.impacts(result.getImpactDescriptors());
		for (ImpactCategoryDescriptor impact : impacts) {
			writer.impactRow(sheet, row, 1, impact);
			List<Double> values = result.getImpactResults(impact);
			writeValues(sheet, row, IMPACT_HEADER.length + 1, values);
			row++;
		}
		for (int i = 0; i < IMPACT_HEADER.length + 7; i++)
			sheet.autoSizeColumn(i);
	}

	private void writeInventorySheet(Workbook workbook) {
		Sheet sheet = workbook.createSheet("Inventory");
		row = 0;
		List<FlowDescriptor> flows = Sort.flows(result.getFlowDescriptors(), cache);
		writeInventorySection(flows, true, sheet);
		writeInventorySection(flows, false, sheet);
		if (!useStreaming) {
			for (int i = 0; i < FLOW_HEADER.length + 7; i++)
				sheet.autoSizeColumn(i);
		}
		flushSheet(sheet);
	}

	private void flushSheet(Sheet sheet) {
		if (!useStreaming)
			return;
		if (!(sheet instanceof SXSSFSheet))
			return;
		SXSSFSheet s = (SXSSFSheet) sheet;
		try {
			log.trace("flush rows of sheet {}", sheet.getSheetName());
			s.flushRows();
		} catch (Exception e) {
			log.error("failed to flush rows of streamed sheet", e);
		}
	}

	private void writeInventorySection(List<FlowDescriptor> flows,
			boolean forInputs, Sheet sheet) {
		writeInventoryHeader(sheet, forInputs);
		FlowIndex idx = result.result.flowIndex;
		for (FlowDescriptor flow : flows) {
			if (idx.isInput(flow.getId()) != forInputs)
				continue;
			writer.flowRow(sheet, row, 1, flow);
			List<Double> values = result.getFlowResults(flow);
			writeValues(sheet, row, FLOW_HEADER.length + 1, values);
			row++;
		}
	}

	private void writeInventoryHeader(Sheet sheet, boolean inputs) {
		row++;
		String section = inputs ? "Inputs" : "Outputs";
		writer.cell(sheet, row, 1, section, true);
		row++;
		writer.headerRow(sheet, row, 1, FLOW_HEADER);
		int nextCol = FLOW_HEADER.length + 1;
		writeValueHeaders(sheet, row, nextCol);
		row++;
	}

	private void writerImpactHeader(Sheet sheet) {
		row++;
		writer.headerRow(sheet, row, 1, IMPACT_HEADER);
		int nextCol = IMPACT_HEADER.length + 1;
		writeValueHeaders(sheet, row, nextCol);
		row++;
	}

	private void writeValueHeaders(Sheet sheet, int row, int startCol) {
		String[] vals = { "Mean", "Standard deviation", "Minimum", "Maximum",
				"Median", "5% Percentile", "95% Percentile" };
		for (int i = 0; i < vals.length; i++)
			writer.cell(sheet, row, startCol + i, vals[i], true);
		int nextCol = startCol + vals.length;
		for (int i = 0; i < result.getNumberOfRuns(); i++)
			writer.cell(sheet, row, nextCol++, "Run " + (i + 1), true);
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
