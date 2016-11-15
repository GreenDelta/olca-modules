package org.openlca.io.xls.results;

import java.awt.Color;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.io.CategoryPair;
import org.openlca.io.DisplayValues;
import org.openlca.io.xls.Excel;

import com.google.common.base.Strings;

/**
 * A helper class for writing values of results to Excel files.
 */
public class CellWriter {

	private final EntityCache cache;
	private final HashMap<Long, String> flowUnits = new HashMap<>();
	private final CellStyles styles;

	public CellWriter(EntityCache cache, Workbook wb) {
		this.cache = cache;
		this.styles = new CellStyles(wb);
	}

	/**
	 * Writes the process information into the given row, starting in column col
	 */
	public void processCol(Sheet sheet, int row, int col, ProcessDescriptor process) {
		cell(sheet, row++, col, process.getRefId(), false);
		cell(sheet, row++, col, process.getName(), false);
		if (process.getLocation() == null)
			return;
		Location loc = cache.get(Location.class, process.getLocation());
		String code = loc == null ? "" : loc.getCode();
		cell(sheet, row++, col, code, false);
	}

	/**
	 * Writes the impact category information into the given row, starting in
	 * column col
	 */
	public void impactRow(Sheet sheet, int row, int col, ImpactCategoryDescriptor impact) {
		cell(sheet, row, col++, impact.getRefId(), false);
		cell(sheet, row, col++, impact.getName(), false);
		cell(sheet, row, col++, impact.getReferenceUnit(), false);
	}

	/**
	 * Writes the given flow information into the given row, starting in column
	 * col
	 */
	public void flowRow(Sheet sheet, int row, int col, FlowDescriptor flow) {
		flow(sheet, row, col, flow, true);
	}

	/**
	 * Writes the given flow information into the given col, starting in row row
	 */
	public void flowCol(Sheet sheet, int row, int col, FlowDescriptor flow) {
		flow(sheet, row, col, flow, false);
	}

	private void flow(Sheet sheet, int row, int col, FlowDescriptor flow, boolean isRow) {
		cell(sheet, isRow ? row : row++, !isRow ? col : col++, flow.getRefId(), false);
		cell(sheet, isRow ? row : row++, !isRow ? col : col++, flow.getName(), false);
		CategoryPair flowCat = CategoryPair.create(flow, cache);
		cell(sheet, isRow ? row : row++, !isRow ? col : col++, flowCat.getCategory(), false);
		cell(sheet, isRow ? row : row++, !isRow ? col : col++, flowCat.getSubCategory(), false);
		cell(sheet, isRow ? row : row++, !isRow ? col : col++, flowUnit(flow), false);
	}

	public int dataQuality(Sheet sheet, int row, int col, double[] quality, RoundingMode rounding, int scores) {
		return dataQuality(sheet, row, col, quality, rounding, scores, false);
	}

	/**
	 * Writes the given data quality information into the given row, starting
	 * with column col
	 */
	private int dataQuality(Sheet sheet, int row, int col, double[] quality, RoundingMode rounding, int scores,
			boolean bold) {
		if (scores == 0 || quality == null)
			return col;
		for (int i = 0; i < quality.length; i++) {
			double value = quality[i];
			if (value == 0d)
				continue;
			int score = (int) (rounding == RoundingMode.CEILING ? Math.ceil(value) : Math.round(value));
			Color color = DQColors.get(score, scores);
			cell(sheet, row, col + i, Integer.toString(score), color, bold);
		}
		return col + quality.length;
	}

	/**
	 * Writes the data quality indicators of the given system into the given
	 * row, starting with column col.
	 */
	public int dataQualityHeader(Sheet sheet, int row, int col, DQSystem system) {
		Collections.sort(system.indicators);
		for (DQIndicator indicator : system.indicators) {
			String name = Integer.toString(indicator.position);
			if (!Strings.isNullOrEmpty(indicator.name)) {
				name = indicator.name.substring(0, 1);
			}
			cell(sheet, row, col++, name, true);
		}
		return col;
	}

	public int headerRow(Sheet sheet, int row, int col, String... vals) {
		if (vals == null)
			return col;
		for (Object val : vals) {
			cell(sheet, row, col++, val, true);
		}
		return col;
	}

	public int headerCol(Sheet sheet, int row, int col, String... vals) {
		if (vals == null)
			return row;
		for (Object val : vals) {
			cell(sheet, row++, col, val, true);
		}
		return row;
	}

	public void cell(Sheet sheet, int row, int col, Object val) {
		cell(sheet, row, col, val, false);
	}

	public void cell(Sheet sheet, int row, int col, Object val, boolean bold) {
		if (bold) {
			cell(sheet, row, col, val, styles.bold());
		} else {
			cell(sheet, row, col, val, (CellStyle) null);
		}
	}

	private void cell(Sheet sheet, int row, int col, Object val, Color color, boolean bold) {
		if (bold) {
			cell(sheet, row, col, val, styles.bold(color));
		} else {
			cell(sheet, row, col, val, styles.normal(color));
		}
	}

	void wrappedCell(Sheet sheet, int row, int col, Object val, Color color, boolean bold) {
		Cell cell = null;
		if (bold) {
			cell = cell(sheet, row, col, val, styles.bold(color));
		} else {
			cell = cell(sheet, row, col, val, styles.normal(color));
		}
		cell.getCellStyle().setWrapText(true);
	}

	private Cell cell(Sheet sheet, int row, int col, Object val, CellStyle style) {
		Cell cell = null;
		if (val instanceof Number) {
			cell = Excel.cell(sheet, row, col, ((Number) val).doubleValue());
		} else if (val instanceof Date) {
			cell = Excel.cell(sheet, row, col);
			cell.setCellValue((Date) val);
			style = styles.date();
		} else {
			cell = Excel.cell(sheet, row, col, val == null ? "" : val.toString());
		}
		cell.setCellStyle(style);
		return cell;
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
