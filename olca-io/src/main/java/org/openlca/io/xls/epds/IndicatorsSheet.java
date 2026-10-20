package org.openlca.io.xls.epds;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.openlca.commons.Strings;
import org.openlca.core.model.Direction;
import org.openlca.core.model.Epd;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Version;
import org.openlca.util.Categories;

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
		sheet.section(headerOf(indicator));
		ctx.addSource(indicator.source);

		sheet.field(Field.UUID, indicator.refId)
			.field(Field.CATEGORY, categoryOf(indicator))
			.field(Field.VERSION, Version.asString(indicator.version))
			.dateTime(Field.LAST_CHANGE, indicator.lastChange > 0
				? new Date(indicator.lastChange)
				: null)
			.field(Field.TAGS, indicator.tags)
			.field(Field.DESCRIPTION, indicator.description)
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

	private static String categoryOf(ImpactCategory indicator) {
		if (indicator.category == null)
			return null;
		var path = Categories.path(indicator.category);
		return path.isEmpty()
			? null
			: String.join("/", path);
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
