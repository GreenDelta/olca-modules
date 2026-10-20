package org.openlca.io.xls.epds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.commons.Strings;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.ImpactCategory;

/// Writes the "EPD - Results" sheet with one row per indicator and one column
/// per module.
class ResultsSheet {

	private static final int MODULE_COL = 3;

	private final WriterContext ctx;
	private final Epd epd;

	ResultsSheet(WriterContext ctx) {
		this.ctx = ctx;
		this.epd = ctx.epd();
	}

	void write() {
		var sheet = ctx.createSheet(Tab.EPD_RESULTS);
		var modules = sortedModules();
		var indicators = indicatorsOf(modules);
		var keys = sortedKeysOf(indicators);
		var values = valuesOf(modules);

		setColumnWidths(sheet, modules.size());
		writeHeader(sheet, modules);

		int rowIdx = 1;
		for (var key : keys) {
			var indicator = indicators.get(key);
			var row = sheet.createRow(rowIdx++);
			text(row, 0, indicator.code);
			text(row, 1, indicator.name);
			text(row, 2, indicator.referenceUnit);

			var moduleValues = values.get(key);
			if (moduleValues == null)
				continue;
			for (var entry : moduleValues.entrySet()) {
				var cell = row.createCell(MODULE_COL + entry.getKey());
				cell.setCellValue(entry.getValue());
				cell.setCellStyle(ctx.styles().plain());
			}
		}
	}

	private List<EpdModule> sortedModules() {
		var modules = new ArrayList<EpdModule>();
		for (var module : epd.modules) {
			if (module != null) {
				modules.add(module);
			}
		}
		modules.sort((m1, m2) -> Strings.compareNatural(m1.name, m2.name));
		return modules;
	}

	private Map<String, ImpactCategory> indicatorsOf(List<EpdModule> modules) {
		var indicators = new HashMap<String, ImpactCategory>();
		for (var module : modules) {
			if (module.result == null)
				continue;
			for (var result : module.result.impactResults) {
				if (result == null || result.indicator == null)
					continue;
				indicators.putIfAbsent(keyOf(result.indicator), result.indicator);
			}
		}
		return indicators;
	}

	private List<String> sortedKeysOf(Map<String, ImpactCategory> indicators) {
		var keys = new ArrayList<>(indicators.keySet());
		keys.sort((k1, k2) ->
			compareIndicators(indicators.get(k1), indicators.get(k2)));
		return keys;
	}

	private int compareIndicators(ImpactCategory i, ImpactCategory j) {
		int c = Strings.compareIgnoreCase(i.code, j.code);
		if (c != 0)
			return c;
		c = Strings.compareIgnoreCase(i.name, j.name);
		if (c != 0)
			return c;
		return Strings.compareIgnoreCase(i.referenceUnit, j.referenceUnit);
	}

	private Map<String, Map<Integer, Double>> valuesOf(List<EpdModule> modules) {
		var values = new HashMap<String, Map<Integer, Double>>();
		for (int i = 0; i < modules.size(); i++) {
			var module = modules.get(i);
			if (module.result == null)
				continue;
			for (var result : module.result.impactResults) {
				if (result == null || result.indicator == null)
					continue;
				var key = keyOf(result.indicator);
				values.computeIfAbsent(key, _ -> new HashMap<>())
					.merge(i, result.amount * module.multiplier, Double::sum);
			}
		}
		return values;
	}

	private static String keyOf(ImpactCategory indicator) {
		if (Strings.isNotBlank(indicator.refId))
			return "id:" + indicator.refId;
		return "code:" + Strings.notNull(indicator.code)
			+ '/' + Strings.notNull(indicator.name)
			+ '/' + Strings.notNull(indicator.referenceUnit);
	}

	private static void setColumnWidths(Sheet sheet, int moduleCount) {
		sheet.setColumnWidth(0, 15 * 256);
		sheet.setColumnWidth(1, 50 * 256);
		sheet.setColumnWidth(2, 15 * 256);
		for (int i = 0; i < moduleCount; i++) {
			sheet.setColumnWidth(MODULE_COL + i, 15 * 256);
		}
	}

	private void writeHeader(Sheet sheet, List<EpdModule> modules) {
		var header = sheet.createRow(0);
		header(header, 0, "Code");
		header(header, 1, "Indicator");
		header(header, 2, "Unit");
		for (int i = 0; i < modules.size(); i++) {
			header(header, MODULE_COL + i, modules.get(i).name);
		}
	}

	private void header(Row row, int col, String value) {
		var cell = row.createCell(col);
		cell.setCellValue(Strings.notNull(value));
		cell.setCellStyle(ctx.styles().bold());
	}

	private void text(Row row, int col, String value) {
		if (value == null)
			return;
		var cell = row.createCell(col);
		cell.setCellValue(value);
		cell.setCellStyle(ctx.styles().plain());
	}
}
