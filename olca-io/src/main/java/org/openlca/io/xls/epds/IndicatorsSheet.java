package org.openlca.io.xls.epds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openlca.commons.Strings;
import org.openlca.core.model.Direction;
import org.openlca.core.model.Epd;
import org.openlca.core.model.ImpactCategory;

/// Writes the "Indicators" sheet with one section per indicator that is used
/// in the module results of the EPD.
class IndicatorsSheet {

	private final WriterContext ctx;
	private final Epd epd;

	IndicatorsSheet(WriterContext ctx) {
		this.ctx = ctx;
		this.epd = ctx.epd();
	}

	void write() {
		var sheet = ctx.writer(Tab.INDICATORS);
		for (var indicator : indicators()) {
			writeIndicator(sheet, indicator);
		}
	}

	private void writeIndicator(SheetWriter sheet, ImpactCategory indicator) {
		ctx.addSource(indicator.source);
		sheet.section(headerOf(indicator))
			.metaDataFieldsOf(indicator)
			.field(Field.CODE, indicator.code)
			.field(Field.SOURCE, indicator.source != null
				? indicator.source.name
				: null)
			.field(Field.REFERENCE_UNIT, indicator.referenceUnit)
			.field(Field.IMPACT_DIRECTION, directionOf(indicator))
			.blank();
	}

	private List<ImpactCategory> indicators() {
		var indicators = new HashMap<String, ImpactCategory>();
		for (var module : epd.modules) {
			if (module == null || module.result == null)
				continue;
			for (var result : module.result.impactResults) {
				if (result == null || result.indicator == null)
					continue;
				indicators.putIfAbsent(
					keyOf(result.indicator), result.indicator);
			}
		}
		var list = new ArrayList<>(indicators.values());
		list.sort(IndicatorsSheet::compare);
		return list;
	}

	private static String keyOf(ImpactCategory indicator) {
		if (Strings.isNotBlank(indicator.refId))
			return "id:" + indicator.refId;
		return "code:" + Strings.notNull(indicator.code)
			+ '/' + Strings.notNull(indicator.name)
			+ '/' + Strings.notNull(indicator.referenceUnit);
	}

	private static int compare(ImpactCategory i, ImpactCategory j) {
		int c = Strings.compareIgnoreCase(i.code, j.code);
		if (c != 0)
			return c;
		c = Strings.compareIgnoreCase(i.name, j.name);
		if (c != 0)
			return c;
		return Strings.compareIgnoreCase(i.referenceUnit, j.referenceUnit);
	}

	private static String headerOf(ImpactCategory indicator) {
		var code = Strings.isBlank(indicator.code)
			? null
			: indicator.code.strip();
		var name = Strings.isBlank(indicator.name)
			? null
			: indicator.name.strip();
		if (code == null)
			return name;
		return name == null
			? code
			: code + " - " + name;
	}

	private static String directionOf(ImpactCategory indicator) {
		var direction = indicator.direction;
		if (direction == null)
			return null;
		return direction == Direction.INPUT
			? "Input"
			: "Output";
	}
}
