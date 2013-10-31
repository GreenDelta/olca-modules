package org.openlca.io.xls.results;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.InventoryResult;
import org.openlca.io.xls.Excel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryResultExport implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private CalculationSetup setup;
	private InventoryResult result;
	private EntityCache cache;
	private File exportFile;
	private Workbook workbook;
	private CellWriter writer;
	private boolean success = false;

	public InventoryResultExport(CalculationSetup setup,
			InventoryResult result, EntityCache cache) {
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
			writer = new CellWriter(cache, workbook);
			writeInfoSheet();
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

	private void writeInfoSheet() {
		Sheet sheet = workbook.createSheet("Info");
		writer.header(sheet, 1, 1, "Quick calculation result");
		writer.header(sheet, 2, 1, "Product system:");
		Excel.cell(sheet, 2, 2, setup.getProductSystem().getName());
		writer.header(sheet, 3, 1, "LCIA Method:");
		if (setup.getImpactMethod() == null)
			Excel.cell(sheet, 3, 2, "none");
		else
			Excel.cell(sheet, 3, 2, setup.getImpactMethod().getName());
		writer.header(sheet, 4, 1, "Normalisation & weighting set:");
		if (setup.getNwSet() == null)
			Excel.cell(sheet, 4, 2, "none");
		else
			Excel.cell(sheet, 3, 2, setup.getNwSet().getReferenceSystem());
		writer.header(sheet, 5, 1, "Allocation method:");
		Excel.cell(sheet, 5, 2, getAllocationMethod());
		writer.header(sheet, 6, 1, "Date:");
		Excel.cell(sheet, 6, 2).setCellValue(new Date());
		Excel.autoSize(sheet, 1, 2);
	}

	private String getAllocationMethod() {
		AllocationMethod method = setup.getAllocationMethod();
		if (method == null)
			return "none";
		switch (method) {
		case CAUSAL:
			return "Causal";
		case ECONOMIC:
			return "economic";
		case NONE:
			return "none";
		case PHYSICAL:
			return "physical";
		case USE_DEFAULT:
			return "process defaults";
		default:
			return "unknown";
		}
	}

	private void writeInventorySheet() {
		Sheet sheet = workbook.createSheet("LCI");
		writer.header(sheet, 1, 1, "Inventory results");
		Set<FlowDescriptor> flowSet = result.getFlowResults().getFlows(cache);
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
		FlowIndex flowIndex = result.getFlowIndex();
		for (FlowDescriptor flow : flows) {
			if (flowIndex.isInput(flow.getId()) != input)
				continue;
			writer.writeFlowRowInfo(sheet, row, flow);
			double val = result.getFlowResult(flow.getId());
			Excel.cell(sheet, row, CellWriter.FLOW_INFO_SIZE + 1, val);
			row++;
		}
		return ++row;
	}

	private void writeImpactSheet() {
		Sheet sheet = workbook.createSheet("LCIA");
		writer.header(sheet, 1, 1, "Impact assessment results");
		Set<ImpactCategoryDescriptor> categorySet = result.getImpactResults()
				.getImpacts(cache);
		List<ImpactCategoryDescriptor> impacts = Utils.sortImpacts(categorySet);
		writer.writeImpactRowHeader(sheet, 2);
		int row = 3;
		for (ImpactCategoryDescriptor impact : impacts) {
			writer.writeImpactRowHeader(sheet, row);
			double val = result.getImpactResult(impact.getId());
			Excel.cell(sheet, row, CellWriter.IMPACT_INFO_SIZE + 1, val);
			row++;
		}
		Excel.autoSize(sheet, 1, CellWriter.IMPACT_INFO_SIZE + 1);
	}
}
