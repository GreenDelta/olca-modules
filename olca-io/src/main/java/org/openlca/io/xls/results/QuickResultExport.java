package org.openlca.io.xls.results;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.ContributionResultProvider;
import org.openlca.io.xls.Excel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickResultExport implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private CalculationSetup setup;
	private ContributionResultProvider<?> result;
	private EntityCache cache;
	private File exportFile;
	private Workbook workbook;
	private CellWriter writer;
	private boolean success = false;

	public QuickResultExport(CalculationSetup setup,
			ContributionResultProvider<?> result,
			EntityCache cache) {
		this.setup = setup;
		this.result = result;
		this.cache = cache;
	}

	public void setExportFile(File exportFile) {
		this.exportFile = exportFile;
	}

	@Override
	public void run() {
		if (exportFile == null) {
			log.warn("No export file set; nothing to do");
			return;
		}
		try (FileOutputStream fos = new FileOutputStream(exportFile)) {
			workbook = new XSSFWorkbook();
			InfoSheet.write(workbook, setup, "Quick calculation result");
			writer = new CellWriter(cache, workbook);
			writeInventorySheet();
			if (result.hasImpactResults())
				writeImpactSheet();
			workbook.write(fos);
			success = true;
		} catch (Exception e) {
			log.error("Export failed", e);
			success = false;
		}
	}

	public boolean doneWithSuccess() {
		return success;
	}

	private void writeInventorySheet() {
		Sheet sheet = workbook.createSheet("LCI");
		writer.header(sheet, 1, 1, "Inventory results");
		Set<FlowDescriptor> flowSet = result.getFlowDescriptors();
		List<FlowDescriptor> flows = Utils.sortFlows(flowSet, cache);
		int nextRow = writeFlowResults(flows, true, 2, sheet);
		writeFlowResults(flows, false, nextRow, sheet);
		Excel.autoSize(sheet, 1, CellWriter.FLOW_INFO_SIZE + 1);
	}

	private int writeFlowResults(List<FlowDescriptor> flows, boolean input,
			int startRow, Sheet sheet) {
		int row = startRow;
		writer.header(sheet, row++, 1, input ? "Inputs" : "Outputs");
		writer.writeFlowRowHeader(sheet, row++);
		FlowIndex flowIndex = result.getResult().getFlowIndex();
		for (FlowDescriptor flow : flows) {
			if (flowIndex.isInput(flow.getId()) != input)
				continue;
			writer.writeFlowRowInfo(sheet, row, flow);
			double val = result.getTotalFlowResult(flow).getValue();
			Excel.cell(sheet, row, CellWriter.FLOW_INFO_SIZE + 1, val);
			row++;
		}
		return ++row;
	}

	private void writeImpactSheet() {
		Sheet sheet = workbook.createSheet("LCIA");
		writer.header(sheet, 1, 1, "Impact assessment results");
		Set<ImpactCategoryDescriptor> categorySet = result
				.getImpactDescriptors();
		List<ImpactCategoryDescriptor> impacts = Utils.sortImpacts(categorySet);
		writer.writeImpactRowHeader(sheet, 2);
		int row = 3;
		for (ImpactCategoryDescriptor impact : impacts) {
			writer.writeImpactRowInfo(sheet, row, impact);
			double val = result.getTotalImpactResult(impact).getValue();
			Excel.cell(sheet, row, CellWriter.IMPACT_INFO_SIZE + 1, val);
			row++;
		}
		Excel.autoSize(sheet, 1, CellWriter.IMPACT_INFO_SIZE + 1);
	}
}
