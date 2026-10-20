package org.openlca.io.xls.epds;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openlca.commons.Strings;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.Result;
import org.openlca.core.model.Version;
import org.openlca.util.Categories;

/// Writes the "EPD - Modules" sheet with one section per module and the
/// metadata of the linked result.
class ModulesSheet {

	private final WriterContext ctx;
	private final Epd epd;

	ModulesSheet(WriterContext ctx) {
		this.ctx = ctx;
		this.epd = ctx.epd();
	}

	void write() {
		var sheet = ctx.writer(Tab.EPD_MODULES);
		for (var module : sortedModules()) {
			writeModule(sheet, module);
		}
	}

	private void writeModule(SheetWriter sheet, EpdModule module) {
		sheet.section(module.name);
		var result = module.result;
		if (result == null) {
			sheet.blank();
			return;
		}

		sheet.field(Field.RESULT, result.name)
			.field(Field.UUID, result.refId);

		if (module.multiplier != 1) {
			sheet.field(Field.RESULT_MULTIPLIER, module.multiplier, null);
		}

		sheet.field(Field.CATEGORY, categoryOf(result))
			.field(Field.VERSION, Version.asString(result.version))
			.dateTime(Field.LAST_CHANGE, result.lastChange > 0
				? new Date(result.lastChange)
				: null)
			.field(Field.TAGS, result.tags)
			.field(Field.DESCRIPTION, result.description);

		if (result.productSystem != null) {
			sheet.field(Field.PRODUCT_SYSTEM, result.productSystem.name);
		}
		if (result.impactMethod != null) {
			sheet.field(Field.LCIA_METHOD, result.impactMethod.name);
		}

		var refFlow = result.referenceFlow;
		sheet.field(Field.REFERENCE_FLOW,
			refFlow != null && refFlow.flow != null
				? refFlow.flow.name
				: null);

		Double amount = refFlow != null
			? refFlow.amount
			: null;
		var unit = refFlow != null && refFlow.unit != null
			? refFlow.unit.name
			: null;
		sheet.field(Field.AMOUNT, amount, unit)
			.blank();
	}

	private static String categoryOf(Result result) {
		if (result.category == null)
			return null;
		var path = Categories.path(result.category);
		return path.isEmpty()
			? null
			: String.join("/", path);
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
}
