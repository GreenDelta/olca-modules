package org.openlca.io.xls.epds;

import java.util.ArrayList;
import java.util.List;

import org.openlca.commons.Strings;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;

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

		sheet.field(Field.RESULT, result.name);

		if (module.multiplier != 1) {
			sheet.field(Field.RESULT_MULTIPLIER, module.multiplier, null);
		}

		sheet.metaDataFieldsOf(result);

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
