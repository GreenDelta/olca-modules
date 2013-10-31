package org.openlca.io.xls.results;

import java.util.HashMap;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.io.CategoryPair;
import org.openlca.io.DisplayValues;
import org.openlca.io.xls.Excel;

/**
 * A helper class for writing values of results to Excel files.
 */
class CellWriter {

	/** Number of attributes of the flow information. */
	static final int FLOW_INFO_SIZE = 5;

	/** Number of attributes of the process information. */
	static final int PROCESS_INFO_SIZE = 3;

	/** Number of attributes of the impact category information. */
	static final int IMPACT_INFO_SIZE = 3;

	private EntityCache cache;
	private CellStyle headerStyle;
	private HashMap<Long, String> flowUnits = new HashMap<>();

	public CellWriter(EntityCache cache, Workbook wb) {
		this.cache = cache;
		this.headerStyle = Excel.headerStyle(wb);
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
		Excel.cell(sheet, row++, col, impact.getRefId());
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
		CategoryPair flowCat = CategoryPair.create(flow, cache);
		Excel.cell(sheet, row, col++, flowCat.getCategory());
		Excel.cell(sheet, row, col++, flowCat.getSubCategory());
		Excel.cell(sheet, row, col++, flowUnit(flow));
	}

	/** Makes a header entry in the given row and column. */
	void header(Sheet sheet, int row, int col, String val) {
		Excel.cell(sheet, row, col, val).setCellStyle(headerStyle);
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
