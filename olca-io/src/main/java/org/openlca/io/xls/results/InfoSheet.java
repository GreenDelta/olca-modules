package org.openlca.io.xls.results;

import java.awt.Color;
import java.math.RoundingMode;
import java.util.Date;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.data_quality.AggregationType;
import org.openlca.core.math.data_quality.DQCalculationSetup;
import org.openlca.core.math.data_quality.ProcessingType;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
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
		boolean withDataQuality = dqSetup != null && dqSetup.exchangeDqSystem != null;
		header(writer, sheet, 1, 1, title, withDataQuality);
		int row = generalInfo(writer, sheet, 2, 2, setup);
		if (dqSetup != null && dqSetup.exchangeDqSystem != null) {
			dataQualityInfo(writer, sheet, row + 2, 2, dqSetup);
			for (int i = 1; i <= dqSetup.exchangeDqSystem.indicators.size() + 1; i++) {
				sheet.setColumnWidth(i, Excel.width(200));
			}
		} else {
			Excel.autoSize(sheet, 1, 2);
		}
	}

	private static void header(CellWriter writer, Sheet sheet, int row, int col, String title, boolean withDataQuality) {
		writer.cell(sheet, row++, col, title, true);
		row = writer.headerCol(sheet, row, col, GENERAL_HEADERS);
		if (!withDataQuality)
			return;
		row++;
		writer.cell(sheet, row++, col, "Data quality", true);
		writer.headerCol(sheet, row++, col, DATA_QUALITY_HEADERS);
	}

	private static int generalInfo(CellWriter writer, Sheet sheet, int row, int col, CalculationSetup setup) {
		ProductSystem system = setup.productSystem;
		writer.cell(sheet, row++, col, system.getName());
		writer.cell(sheet, row++, col, process(system));
		writer.cell(sheet, row++, col, location(system));
		writer.cell(sheet, row++, col, product(system));
		writer.cell(sheet, row++, col, amount(system));
		writer.cell(sheet, row++, col, method(setup));
		writer.cell(sheet, row++, col, nwSet(setup));
		writer.cell(sheet, row++, col, allocation(setup));
		writer.cell(sheet, row++, col, cutoff(system));
		writer.cell(sheet, row++, col, new Date());
		return row;
	}

	private static void dataQualityInfo(CellWriter writer, Sheet sheet, int row, int col, DQCalculationSetup setup) {
		writer.cell(sheet, row++, col, dqSystem(setup.exchangeDqSystem));
		writer.cell(sheet, row++, col, aggregation(setup));
		writer.cell(sheet, row++, col, rounding(setup));
		writer.cell(sheet, row++, col, processing(setup));
		legend(writer, sheet, row + 1, col - 1, setup);
	}

	private static void legend(CellWriter writer, Sheet sheet, int row, int col, DQCalculationSetup setup) {
		for (DQIndicator indicator : setup.exchangeDqSystem.indicators) {
			writer.cell(sheet, row, col + indicator.position, indicator(indicator), true);
		}
		for (DQScore score : setup.exchangeDqSystem.indicators.get(0).scores) {
			writer.cell(sheet, row + score.position, col, score(score), true);
		}
		for (DQIndicator indicator : setup.exchangeDqSystem.indicators) {
			for (DQScore score : indicator.scores) {
				Color color = DQColors.get(score.position, setup.exchangeDqSystem.getScoreCount());
				writer.wrappedCell(sheet, row + score.position, col + indicator.position, score.description, color,
						true);
			}
		}
	}

	private static String process(ProductSystem system) {
		Process p = system.referenceProcess;
		if (p == null)
			return "";
		return p.getName();
	}

	private static String location(ProductSystem system) {
		Process p = system.referenceProcess;
		if (p == null || p.getLocation() == null)
			return "";
		return p.getLocation().getName();
	}

	private static String product(ProductSystem system) {
		Exchange e = system.referenceExchange;
		if (e == null || e.flow == null)
			return "";
		return e.flow.getName();
	}

	private static String amount(ProductSystem system) {
		if (system.targetUnit == null)
			return "";
		return system.targetAmount + " " + system.targetUnit.getName();
	}

	private static String method(CalculationSetup setup) {
		if (setup.impactMethod == null)
			return "none";
		return setup.impactMethod.getName();
	}

	private static String nwSet(CalculationSetup setup) {
		if (setup.nwSet == null)
			return "none";
		return setup.nwSet.getName();
	}

	private static String allocation(CalculationSetup setup) {
		AllocationMethod method = setup.allocationMethod;
		if (method == null)
			return "none";
		switch (method) {
		case CAUSAL:
			return "causal";
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

	private static String cutoff(ProductSystem system) {
		if (system.cutoff == null)
			return "none";
		return Double.toString(system.cutoff);
	}

	private static String dqSystem(DQSystem system) {
		if (system == null)
			return "none";
		return system.getName();
	}

	private static String aggregation(DQCalculationSetup setup) {
		AggregationType type = setup.aggregationType;
		if (type == null)
			return "none";
		switch (type) {
		case WEIGHTED_AVERAGE:
			return "weighted average";
		case WEIGHTED_SQUARED_AVERAGE:
			return "weighted squared average";
		case MAXIMUM:
			return "maximum";
		case NONE:
			return "none";
		default:
			return "unknown";
		}
	}

	private static String rounding(DQCalculationSetup setup) {
		RoundingMode mode = setup.roundingMode;
		if (mode == null)
			return "none";
		switch (mode) {
		case CEILING:
			return "up";
		case HALF_UP:
			return "half up";
		default:
			return "unknown";
		}
	}

	private static String processing(DQCalculationSetup setup) {
		ProcessingType type = setup.processingType;
		if (type == null)
			return "none";
		switch (type) {
		case EXCLUDE:
			return "exclude zero values";
		case USE_MAX:
			return "use maximum score for zero values";
		default:
			return "unknown";
		}
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
