package org.openlca.io.xls.results;

import java.awt.Color;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Location;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
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
	public void processCol(Sheet sheet, int row, int col,
			RootDescriptor process) {
		cell(sheet, row++, col, process.refId, false);
		cell(sheet, row++, col, process.name, false);
		if (!(process instanceof ProcessDescriptor))
			return;
		ProcessDescriptor p = (ProcessDescriptor) process;
		if (p.location == null)
			return;
		Location loc = cache.get(Location.class, p.location);
		String code = loc == null ? "" : loc.code;
		cell(sheet, row++, col, code, false);
	}

	/**
	 * Writes the impact category information into the given row, starting in
	 * column col
	 */
	public int impactRow(Sheet sheet, int row, int col, ImpactDescriptor impact) {
		cell(sheet, row, col++, impact.refId, false);
		cell(sheet, row, col++, impact.name, false);
		cell(sheet, row, col++, impact.referenceUnit, false);
		return col;
	}

	public int impactNwRow(Sheet sheet, int row, int col, ImpactDescriptor impact, double value, NwSet nwSet) {
		NwFactor nwFactor = nwSet.getFactor(impact);
		if (nwFactor == null)
			return col;
		double normalizedValue = value * (nwFactor.normalisationFactor == null || nwFactor.normalisationFactor == 0 ? 0
				: 1 / nwFactor.normalisationFactor);
		double weightedValue = normalizedValue * (nwFactor.weightingFactor == null ? 0 : nwFactor.weightingFactor);
		cell(sheet, row, col++, normalizedValue, false);
		cell(sheet, row, col++, weightedValue, false);
		cell(sheet, row, col++, nwSet.weightedScoreUnit, false);
		return col;
	}

	/**
	 * Writes the given flow information into the given row, starting in column
	 * col
	 */
	public void flowRow(Sheet sheet, int row, int col, EnviFlow flow) {
		flow(sheet, row, col, flow, true);
	}

	/**
	 * Writes the given flow information into the given col, starting in row row
	 */
	public void flowCol(Sheet sheet, int row, int col, EnviFlow flow) {
		flow(sheet, row, col, flow, false);
	}

	private void flow(Sheet sheet, int row, int col, EnviFlow flow, boolean isRow) {
		if (flow == null || flow.flow() == null)
			return;
		FlowDescriptor f = flow.flow();
		cell(sheet, isRow ? row : row++, !isRow ? col : col++, f.refId, false);
		cell(sheet, isRow ? row : row++, !isRow ? col : col++, f.name, false);
		CategoryPair flowCat = CategoryPair.create(f, cache);
		cell(sheet, isRow ? row : row++, !isRow ? col : col++,
				flowCat.getCategory(), false);
		cell(sheet, isRow ? row : row++, !isRow ? col : col++,
				flowCat.getSubCategory(), false);
		cell(sheet, isRow ? row : row++, !isRow ? col : col++, flowUnit(f), false);
	}

	/**
	 * Writes the given data quality information into the given row, starting
	 * with column col
	 */
	public int dataQuality(Sheet sheet, int row, int col,
			int[] result, DQSystem system) {
		if (result == null || system == null)
			return col;
		int n = system.getScoreCount();
		if (n == 0)
			return col;
		for (int i = 0; i < result.length; i++) {
			int score = result[i];
			if (score <= 0)
				continue;
			if (score > n) {
				score = n;
			}
			Color color = DQColors.get(score, n);
			String label = system.getScoreLabel(score);
			label = label == null ? "" : label;
			cell(sheet, row, col + i, label, color, false);
		}
		return col + result.length;
	}

	/**
	 * Writes the data quality indicators of the given system into the given
	 * row, starting with column col.
	 */
	public int dataQualityHeader(Sheet sheet, int row, int col,
			DQSystem system) {
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

	private void cell(Sheet sheet, int row, int col, Object val, Color color,
			boolean bold) {
		if (bold) {
			cell(sheet, row, col, val, styles.bold(color));
		} else {
			cell(sheet, row, col, val, styles.normal(color));
		}
	}

	void boldWrapped(Sheet sheet, int row, int col, Object val, Color color) {
		var cell = cell(sheet, row, col, val, styles.bold(color));
		cell.ifPresent(c -> c.getCellStyle().setWrapText(true));
	}

	private Optional<Cell> cell(Sheet sheet, int row, int col, Object val, CellStyle style) {
		Optional<Cell> cell;
		if (val instanceof Number) {
			cell = Excel.cell(sheet, row, col, ((Number) val).doubleValue());
		} else if (val instanceof Date) {
			cell = Excel.cell(sheet, row, col);
			cell.ifPresent(c -> c.setCellValue((Date) val));
			style = styles.date();
		} else {
			cell = Excel.cell(sheet, row, col,
					val == null ? "" : val.toString());
		}
		if (cell.isPresent()) {
			cell.get().setCellStyle(style);
		}
		return cell;
	}

	private String flowUnit(FlowDescriptor flow) {
		String unit = flowUnits.get(flow.id);
		if (unit != null)
			return unit;
		unit = DisplayValues.referenceUnit(flow, cache);
		flowUnits.put(flow.id, unit == null ? "" : unit);
		return unit;
	}

}
