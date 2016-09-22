package org.openlca.io.xls.results;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.FullResultProvider;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports an analysis result to Excel. Because of the size of the results we
 * use the POI streaming API here (SXSSF, see
 * http://poi.apache.org/spreadsheet/how-to.html#sxssf). After a sheet is filled
 * we flush its rows which means that these rows are written to disk and not
 * accessible from memory any more.
 */
public class AnalysisResultExport implements IExcelExport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private CalculationSetup setup;
	private File file;
	private FullResultProvider result;

	private SXSSFWorkbook workbook;
	private CellWriter writer;
	private List<ProcessDescriptor> processes;
	private List<ImpactCategoryDescriptor> impacts;
	private List<FlowDescriptor> flows;

	private boolean success = false;

	public AnalysisResultExport(CalculationSetup setup, FullResultProvider result) {
		this.setup = setup;
		this.result = result;
	}

	@Override
	public void setFile(File file) {
		this.file = file;
	}

	@Override
	public void run() {
		try {
			prepareFlowInfos();
			prepareProcesses();
			prepareImpacts();
			workbook = new SXSSFWorkbook(-1); // no default flushing (see
			// Excel.cell)!
			writer = new CellWriter(result.cache, workbook);
			InfoSheet.write(workbook, setup, "Analysis result");
			writeInventorySheets(result);
			writeImpactSheets(result);
			try (FileOutputStream fos = new FileOutputStream(file)) {
				workbook.write(fos);
				fos.flush();
			}
			success = true;
		} catch (Exception e) {
			log.error("Export failed", e);
			success = false;
		}
	}

	public boolean doneWithSuccess() {
		return success;
	}

	CellWriter getWriter() {
		return writer;
	}

	private void writeInventorySheets(FullResultProvider result) {
		SXSSFSheet lciSheet = sheet("LCI (total)");
		fillTotalInventory(lciSheet);
		flush(lciSheet);
		SXSSFSheet lciConSheet = sheet("LCI (contributions)");
		SingleProcessInventories.write(lciConSheet, result, this);
		flush(lciConSheet);
	}

	private void writeImpactSheets(FullResultProvider result) {
		if (!result.hasImpactResults())
			return;
		SXSSFSheet totalImpactSheet = sheet("LCIA (total)");
		TotalImpacts.write(totalImpactSheet, result, this);
		flush(totalImpactSheet);
		SXSSFSheet singleImpactSheet = sheet("LCIA (contributions)");
		ProcessImpacts.write(singleImpactSheet, result, this);
		flush(singleImpactSheet);
		SXSSFSheet flowImpactSheet = sheet("LCIA (flows)");
		FlowImpacts.write(flowImpactSheet, result, this);
		flush(flowImpactSheet);
	}

	private SXSSFSheet sheet(String name) {
		return (SXSSFSheet) workbook.createSheet(name);
	}

	private void flush(SXSSFSheet sheet) {
		log.trace("flush sheet {}", sheet);
		try {
			sheet.flushRows();
		} catch (Exception e) {
			log.error("Failed to flush rows");
		}
	}

	private void prepareFlowInfos() {
		Set<FlowDescriptor> set = result.getFlowDescriptors();
		flows = Utils.sortFlows(set, result.cache);
	}

	private void prepareProcesses() {
		Set<ProcessDescriptor> procs = result.getProcessDescriptors();
		processes = new ArrayList<>(procs);
		long refProcess = result.result.productIndex.getRefFlow().getFirst();
		Collections.sort(processes, (d1, d2) -> {
			if (d1.getId() == refProcess)
				return -1;
			if (d2.getId() == refProcess)
				return 1;
			return Strings.compare(d1.getName(), d2.getName());
		});
	}

	private void prepareImpacts() {
		if (!result.hasImpactResults())
			return;
		Set<ImpactCategoryDescriptor> set = result.getImpactDescriptors();
		impacts = Utils.sortImpacts(set);
	}

	/**
	 * Visit the sorted flows of the analysis result.
	 */
	void visitFlows(FlowVisitor visitor) {
		FlowIndex index = result.result.flowIndex;
		for (FlowDescriptor flow : flows) {
			visitor.next(flow, index.isInput(flow.getId()));
		}
	}

	/**
	 * Returns the sorted processes of the result, the reference process is at
	 * the first location.
	 */
	List<ProcessDescriptor> getProcesses() {
		return processes;
	}

	/**
	 * Returns the sorted impact assessment categories of the result.
	 */
	List<ImpactCategoryDescriptor> getImpacts() {
		if (impacts == null)
			Collections.emptyList();
		return impacts;
	}

	private void fillTotalInventory(Sheet sheet) {
		int row = 1;
		row = writeTotalResults(sheet, row, true);
		writeTotalResults(sheet, row + 2, false);
		// Excel.autoSize(sheet, 1, 2, 3, 4, 5, 6);
	}

	private int writeTotalResults(Sheet sheet, int startRow, boolean inputs) {
		FlowIndex flowIndex = result.result.flowIndex;
		int rowNo = startRow;
		String section = inputs ? "Inputs" : "Outputs";
		writer.header(sheet, rowNo++, 1, section);
		writer.writeFlowRowHeader(sheet, rowNo);
		writer.header(sheet, rowNo++, 6, "Result");
		for (FlowDescriptor flow : flows) {
			boolean input = flowIndex.isInput(flow.getId());
			if (input != inputs)
				continue;
			double amount = result.getTotalFlowResult(flow).value;
			if (amount == 0)
				continue;
			writer.writeFlowRowInfo(sheet, rowNo, flow);
			Excel.cell(sheet, rowNo, 6, amount);
			rowNo++;
		}
		return rowNo;
	}

	/**
	 * Visitor for the flows in the analysis result.
	 */
	interface FlowVisitor {
		void next(FlowDescriptor flow, boolean input);
	}

}
