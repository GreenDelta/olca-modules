package org.openlca.io.xls.results;

import java.awt.Color;
import java.util.Date;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.math.data_quality.DQCalculationSetup;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.io.xls.Excel;

import com.google.common.base.Strings;

public class InfoSheet {

	private static final String[] GENERAL_HEADERS = {
			"Product system:",
			"Reference process:",
			"Reference process location:",
			"Product:",
			"Amount:",
			"Impact method:",
			"Normalisation & weighting set:",
			"Allocation method:",
			"Cutoff:",
			"Date:"
	};

	private static final String[] DATA_QUALITY_HEADERS = {
			"Data quality schema:",
			"Aggregation type:",
			"Rounding mode:",
			"n.a. value handling:"
	};

	public static void write(Workbook workbook, CellWriter writer, CalculationSetup setup, DQCalculationSetup dqSetup,
			String title) {
		Sheet sheet = workbook.createSheet("Calculation setup");
		Excel.trackSize(sheet, 1, 2);
		boolean withDataQuality = dqSetup != null && dqSetup.exchangeSystem != null;
		header(writer, sheet, 1, title, withDataQuality);
		int row = generalInfo(writer, sheet, 2, setup);
		if (dqSetup != null && dqSetup.exchangeSystem != null) {
			dataQualityInfo(writer, sheet, row + 2, dqSetup);
			for (int i = 1; i <= dqSetup.exchangeSystem.indicators.size() + 1; i++) {
				sheet.setColumnWidth(i, Excel.width(200));
			}
		} else {
			Excel.autoSize(sheet, 1, 2);
		}
	}

	private static void header(CellWriter writer, Sheet sheet, int row, String title, boolean withDataQuality) {
		writer.cell(sheet, row++, 1, title, true);
		row = writer.headerCol(sheet, row, 1, GENERAL_HEADERS);
		if (!withDataQuality)
			return;
		row++;
		writer.cell(sheet, row++, 1, "Data quality", true);
		writer.headerCol(sheet, row++, 1, DATA_QUALITY_HEADERS);
	}

	private static int generalInfo(
		CellWriter writer, Sheet sheet, int row, CalculationSetup setup) {
		var name = setup.hasProductSystem()
			? setup.productSystem().name
			: setup.process().name;
		writer.cell(sheet, row++, 2, name);
		writer.cell(sheet, row++, 2, process(setup));
		writer.cell(sheet, row++, 2, location(setup));
		writer.cell(sheet, row++, 2, product(setup));
		writer.cell(sheet, row++, 2, amount(setup));
		writer.cell(sheet, row++, 2, method(setup));
		writer.cell(sheet, row++, 2, nwSet(setup));
		writer.cell(sheet, row++, 2, allocation(setup));
		writer.cell(sheet, row++, 2, cutoff(setup));
		writer.cell(sheet, row++, 2, new Date());
		return row;
	}

	private static void dataQualityInfo(
		CellWriter writer, Sheet sheet, int row, DQCalculationSetup setup) {
		writer.cell(sheet, row++, 2, dqSystem(setup.exchangeSystem));
		writer.cell(sheet, row++, 2, aggregation(setup));
		writer.cell(sheet, row++, 2, setup.ceiling ? "up" : "half up");
		writer.cell(sheet, row++, 2, naHandling(setup));
		legend(writer, sheet, row + 1, setup);
	}

	private static void legend(
		CellWriter writer, Sheet sheet, int row, DQCalculationSetup setup) {
		for (DQIndicator indicator : setup.exchangeSystem.indicators) {
			writer.cell(sheet, row, 1 + indicator.position, indicator(indicator), true);
		}
		for (DQScore score : setup.exchangeSystem.indicators.get(0).scores) {
			writer.cell(sheet, row + score.position, 1, score(score), true);
		}
		for (DQIndicator indicator : setup.exchangeSystem.indicators) {
			for (DQScore score : indicator.scores) {
				Color color = DQColors.get(
						score.position,
						setup.exchangeSystem.getScoreCount());
				writer.boldWrapped(
						sheet,
						row + score.position,
						1 + indicator.position,
						score.description,
						color);
			}
		}
	}

	private static String process(CalculationSetup setup) {
		var p = setup.process();
		return p == null
			? ""
			: p.name;
	}

	private static String location(CalculationSetup setup) {
		var p = setup.process();
		if (p == null || p.location == null)
			return "";
		return p.location.name;
	}

	private static String product(CalculationSetup setup) {
		var flow = setup.flow();
		return flow == null
			? ""
			: flow.name;
	}

	private static String amount(CalculationSetup setup) {
		var unit = setup.unit();
		if (unit == null)
			return "";
		return setup.amount() + " " + unit.name;
	}

	private static String method(CalculationSetup setup) {
		var method = setup.impactMethod();
		return method == null
			? "none"
			: method.name;
	}

	private static String nwSet(CalculationSetup setup) {
		var nwSet = setup.nwSet();
		return nwSet == null
			? "none"
			: nwSet.name;
	}

	private static String allocation(CalculationSetup setup) {
		var method = setup.allocation();
		if (method == null)
			return "none";
		return switch (method) {
			case CAUSAL -> "causal";
			case ECONOMIC -> "economic";
			case NONE -> "none";
			case PHYSICAL -> "physical";
			case USE_DEFAULT -> "process defaults";
		};
	}

	private static String cutoff(CalculationSetup setup) {
		if (!setup.hasProductSystem())
			return "none";
		var system = setup.productSystem();
		return system.cutoff == null
			? "none"
			: Double.toString(system.cutoff);
	}

	private static String dqSystem(DQSystem system) {
		if (system == null)
			return "none";
		return system.name;
	}

	private static String aggregation(DQCalculationSetup setup) {
		var type = setup.aggregationType;
		if (type == null)
			return "none";
		return switch (type) {
			case WEIGHTED_AVERAGE -> "weighted average";
			case WEIGHTED_SQUARED_AVERAGE -> "weighted squared average";
			case MAXIMUM -> "maximum";
			case NONE -> "none";
		};
	}

	private static String naHandling(DQCalculationSetup setup) {
		var type = setup.naHandling;
		if (type == null)
			return "none";
		return switch (type) {
			case EXCLUDE -> "exclude zero values";
			case USE_MAX -> "use maximum score for zero values";
		};
	}

	private static String indicator(DQIndicator indicator) {
		if (Strings.isNullOrEmpty(indicator.name))
			return Integer.toString(indicator.position);
		return indicator.name;
	}

	private static String score(DQScore score) {
		if (Strings.isNullOrEmpty(score.label))
			return Integer.toString(score.position);
		return score.label;
	}
}
