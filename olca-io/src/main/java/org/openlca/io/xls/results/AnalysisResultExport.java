package org.openlca.io.xls.results;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.matrices.FlowIndex;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Location;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.AnalysisResult;
import org.openlca.io.CategoryPair;
import org.openlca.io.DisplayValues;
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
public class AnalysisResultExport {

	/** Number of attributes of the flow information. */
	final int FLOW_INFO_SIZE = 5;

	/** Number of attributes of the process information. */
	final int PROCESS_INFO_SIZE = 3;

	/** Number of attributes of the impact category information. */
	final int IMPACT_INFO_SIZE = 3;

	private Logger log = LoggerFactory.getLogger(getClass());
	private ProductSystem system;
	private File file;
	private EntityCache cache;
	private AnalysisResult result;
	private CellStyle headerStyle;
	private SXSSFWorkbook workbook;

	private List<ProcessDescriptor> processes;
	private List<ImpactCategoryDescriptor> impacts;
	private List<FlowDescriptor> flows;

	private HashMap<Long, CategoryPair> flowCategories = new HashMap<>();
	private HashMap<Long, String> flowUnits = new HashMap<>();

	public AnalysisResultExport(ProductSystem system, File file,
			EntityCache cache) {
		this.system = system;
		this.file = file;
		this.cache = cache;
	}

	EntityCache getCache() {
		return cache;
	}

	public void run(AnalysisResult result) throws Exception {
		this.result = result;
		prepareFlowInfos();
		prepareProcesses();
		prepareImpacts();
		workbook = new SXSSFWorkbook(-1); // no default flushing (see
											// Excel.cell)!
		headerStyle = Excel.headerStyle(workbook);
		writeInventorySheets(result);
		writeImpactSheets(result);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			workbook.write(fos);
			fos.flush();
		}
	}

	private void writeInventorySheets(AnalysisResult result) {
		SXSSFSheet infoSheet = sheet("info");
		fillInfoSheet(infoSheet);
		flush(infoSheet);
		SXSSFSheet lciSheet = sheet("LCI (total)");
		fillTotalInventory(lciSheet);
		flush(lciSheet);
		SXSSFSheet lciConSheet = sheet("LCI (contributions)");
		AnalysisProcessInventories.write(lciConSheet, result, this);
		flush(lciConSheet);
	}

	private void writeImpactSheets(AnalysisResult result) {
		if (!result.hasImpactResults())
			return;
		SXSSFSheet totalImpactSheet = sheet("LCIA (total)");
		AnalysisTotalImpact.write(totalImpactSheet, result, this);
		flush(totalImpactSheet);
		SXSSFSheet singleImpactSheet = sheet("LCIA (contributions)");
		AnalysisProcessImpacts.write(singleImpactSheet, result, this);
		flush(singleImpactSheet);
		SXSSFSheet flowImpactSheet = sheet("LCIA (flows)");
		AnalysisFlowImpacts.write(flowImpactSheet, result, this);
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
		Set<FlowDescriptor> set = result.getFlowResults().getFlows(cache);
		flows = new ArrayList<>(set);
		Collections.sort(flows, new Comparator<FlowDescriptor>() {
			@Override
			public int compare(FlowDescriptor o1, FlowDescriptor o2) {
				CategoryPair cat1 = flowCategory(o1);
				CategoryPair cat2 = flowCategory(o2);
				int c = cat1.compareTo(cat2);
				if (c != 0)
					return c;
				return Strings.compare(o1.getName(), o2.getName());
			}
		});
	}

	private void prepareProcesses() {
		Set<ProcessDescriptor> procs = result.getFlowResults().getProcesses(
				cache);
		processes = new ArrayList<>(procs);
		final long refProcess = result.getProductIndex().getRefProduct()
				.getFirst();
		Collections.sort(processes, new Comparator<ProcessDescriptor>() {
			@Override
			public int compare(ProcessDescriptor o1, ProcessDescriptor o2) {
				if (o1.getId() == refProcess)
					return -1;
				if (o2.getId() == refProcess)
					return 1;
				return Strings.compare(o1.getName(), o2.getName());
			}
		});
	}

	private void prepareImpacts() {
		if (!result.hasImpactResults())
			return;
		Set<ImpactCategoryDescriptor> set = result.getImpactResults()
				.getImpacts(cache);
		impacts = new ArrayList<>(set);
		Collections.sort(impacts, new Comparator<ImpactCategoryDescriptor>() {
			@Override
			public int compare(ImpactCategoryDescriptor d1,
					ImpactCategoryDescriptor d2) {
				return Strings.compare(d1.getName(), d2.getName());
			}
		});
	}

	/** Get the header style of the workbook. */
	CellStyle getHeaderStyle() {
		return headerStyle;
	}

	/** Visit the sorted flows of the analysis result. */
	void visitFlows(FlowVisitor visitor) {
		FlowIndex index = result.getFlowIndex();
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

	/** Returns the sorted impact assessment categories of the result. */
	List<ImpactCategoryDescriptor> getImpacts() {
		if (impacts == null)
			Collections.emptyList();
		return impacts;
	}

	private void fillInfoSheet(Sheet sheet) {
		Exchange refExchange = system.getReferenceExchange();
		header(sheet, 1, 1, "Analysis result");
		header(sheet, 2, 1, "Product system");
		Excel.cell(sheet, 2, 2, system.getName());
		header(sheet, 3, 1, "Demand - product");
		Excel.cell(sheet, 3, 2, refExchange.getFlow().getName());
		header(sheet, 4, 1, "Demand - value");
		Excel.cell(sheet, 4, 2, system.getTargetAmount() + " "
				+ system.getTargetUnit().getName());
		// Excel.autoSize(sheet, 1, 2);
	}

	private void fillTotalInventory(Sheet sheet) {
		int row = 1;
		row = writeTotalResults(sheet, row, true);
		writeTotalResults(sheet, row + 2, false);
		// Excel.autoSize(sheet, 1, 2, 3, 4, 5, 6);
	}

	private int writeTotalResults(Sheet sheet, int startRow, boolean inputs) {
		long refProcess = result.getProductIndex().getRefProduct().getFirst();
		FlowIndex flowIndex = result.getFlowIndex();
		int rowNo = startRow;
		String section = inputs ? "Inputs" : "Outputs";
		Excel.cell(sheet, rowNo++, 1, section).setCellStyle(headerStyle);
		writeFlowRowHeader(sheet, rowNo);
		Excel.cell(sheet, rowNo++, 6, "Result").setCellStyle(headerStyle);
		for (FlowDescriptor flow : flows) {
			boolean input = flowIndex.isInput(flow.getId());
			if (input != inputs)
				continue;
			double amount = result.getTotalFlowResult(refProcess, flow.getId());
			if (amount == 0)
				continue;
			writeFlowRowInfo(sheet, rowNo, flow);
			Excel.cell(sheet, rowNo, 6, amount);
			rowNo++;
		}
		return rowNo;
	}

	/**
	 * Writes the process information header into the given column starting at
	 * row 1. The next free row is 1 + PROCESS_INFO_SIZE.
	 */
	void writeProcessColHeader(Sheet sheet, int col) {
		int row = 1;
		header(sheet, row++, col, "Process UUID");
		header(sheet, row++, col, "Process");
		header(sheet, row++, col, "Location");
	}

	/**
	 * Writes the process information header into the given row starting at
	 * column 1. The next free column is 1 + PROCESS_INFO_SIZE.
	 */
	void writeProcessRowHeader(Sheet sheet, int row) {
		int col = 1;
		header(sheet, row, col++, "Process UUID");
		header(sheet, row, col++, "Process");
		header(sheet, row, col++, "Location");
	}

	/** Writes the process information into the given column starting at row 1. */
	void writeProcessColInfo(Sheet sheet, int col, ProcessDescriptor process) {
		int row = 1;
		Excel.cell(sheet, row++, col, process.getRefId());
		Excel.cell(sheet, row++, col, process.getName());
		if (process.getLocation() != null) {
			Location loc = cache.get(Location.class, process.getLocation());
			String code = loc == null ? "" : loc.getCode();
			Excel.cell(sheet, row, col, code);
		}
	}

	/** Writes the process information into the given row starting at column 1. */
	void writeProcessRowInfo(Sheet sheet, int row, ProcessDescriptor process) {
		int col = 1;
		Excel.cell(sheet, row, col++, process.getRefId());
		Excel.cell(sheet, row, col++, process.getName());
		if (process.getLocation() != null) {
			Location loc = cache.get(Location.class, process.getLocation());
			String code = loc == null ? "" : loc.getCode();
			Excel.cell(sheet, row, col++, code);
		}
	}

	/**
	 * Writes the impact category header into the given column starting at row
	 * 1. The next free row is 1 + IMPACT_INFO_SIZE.
	 */
	void writeImpactColHeader(Sheet sheet, int col) {
		int row = 1;
		header(sheet, row++, col, "Impact category UUID");
		header(sheet, row++, col, "Impact category");
		header(sheet, row++, col, "Reference unit");
	}

	/**
	 * Writes the impact category information into the given column starting at
	 * row 1.
	 */
	void writeImpactColInfo(Sheet sheet, int col,
			ImpactCategoryDescriptor impact) {
		int row = 1;
		Excel.cell(sheet, row++, col, impact.getId());
		Excel.cell(sheet, row++, col, impact.getName());
		Excel.cell(sheet, row++, col, impact.getReferenceUnit());
	}

	/**
	 * Writes the impact category header into the given row starting at column
	 * 1. The next free column is 1 + IMPACT_INFO_SIZE.
	 */
	void writeImpactRowHeader(Sheet sheet, int row) {
		int col = 1;
		header(sheet, row, col++, "Impact category UUID");
		header(sheet, row, col++, "Impact category");
		header(sheet, row, col++, "Reference unit");
	}

	/**
	 * Writes the impact category information into the given row starting at
	 * column 1.
	 */
	void writeImpactRowInfo(Sheet sheet, int row,
			ImpactCategoryDescriptor impact) {
		int col = 1;
		Excel.cell(sheet, row, col++, impact.getId());
		Excel.cell(sheet, row, col++, impact.getName());
		Excel.cell(sheet, row, col++, impact.getReferenceUnit());
	}

	/**
	 * Writes the flow-information header into the given row starting at column
	 * 1. The next free column is 1 + FLOW_INFO_SIZE.
	 */
	void writeFlowRowHeader(Sheet sheet, int row) {
		int col = 1;
		header(sheet, row, col++, "Flow UUID");
		header(sheet, row, col++, "Flow");
		header(sheet, row, col++, "Category");
		header(sheet, row, col++, "Sub-category");
		header(sheet, row, col++, "Unit");
	}

	/**
	 * Writes the given flow information into the given row starting at column
	 * 1. The next free column is 1 + FLOW_INFO_SIZE.
	 */
	void writeFlowRowInfo(Sheet sheet, int row, FlowDescriptor flow) {
		int col = 1;
		Excel.cell(sheet, row, col++, flow.getRefId());
		Excel.cell(sheet, row, col++, flow.getName());
		CategoryPair flowCat = flowCategory(flow);
		Excel.cell(sheet, row, col++, flowCat.getCategory());
		Excel.cell(sheet, row, col++, flowCat.getSubCategory());
		Excel.cell(sheet, row, col++, flowUnit(flow));
	}

	/** Makes a header entry in the given row and column. */
	void header(Sheet sheet, int row, int col, String val) {
		Excel.cell(sheet, row, col, val).setCellStyle(headerStyle);
	}

	/** Visitor for the flows in the analysis result. */
	interface FlowVisitor {
		void next(FlowDescriptor flow, boolean input);
	}

	private CategoryPair flowCategory(FlowDescriptor flow) {
		CategoryPair pair = flowCategories.get(flow.getId());
		if (pair != null)
			return pair;
		Long catId = flow.getCategory();
		if (catId == null)
			pair = new CategoryPair(null);
		else {
			Category cat = cache.get(Category.class, catId);
			pair = new CategoryPair(cat);
		}
		flowCategories.put(flow.getId(), pair);
		return pair;
	}

	private String flowUnit(FlowDescriptor flow) {
		String unit = flowUnits.get(flow.getId());
		if (unit != null)
			return unit;
		unit = DisplayValues.referenceUnit(flow, cache);
		flowUnits.put(flow.getId(), unit == null ? "" : unit);
		return unit;
	}

}
