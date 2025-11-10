package org.openlca.io.xls.process;

import java.util.ArrayList;
import java.util.List;

import org.openlca.commons.Strings;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;

// TODO: global parameters can have more meta-data (category etc.)
class OutParameterSync {

	private final OutConfig config;

	private OutParameterSync(OutConfig config) {
		this.config = config;
	}

	static void sync(OutConfig config) {
		new OutParameterSync(config).write();
	}

	private void write() {
		var sheet = config.createSheet(Tab.PARAMETERS)
				.withColumnWidths(7, 25);
		writeGlobalParams(sheet);

		var inpParams = new ArrayList<Parameter>();
		var depParams = new ArrayList<Parameter>();
		config.process().parameters.stream()
			.sorted((p1, p2) -> Strings.compareIgnoreCase(p1.name, p2.name))
					.forEach(p -> {
						var list = p.isInputParameter
							? inpParams
							: depParams;
						list.add(p);
					});

		sheet.next(Section.INPUT_PARAMETERS);
		writeInputParams(sheet, inpParams);
		sheet.next();

		sheet.next(Section.CALCULATED_PARAMETERS);
		writeDependentParams(sheet, depParams);
	}

	private void writeGlobalParams(SheetWriter sheet) {
		var inputParams = new ArrayList<Parameter>();
		var calcParams = new ArrayList<Parameter>();
		config.db().getAll(Parameter.class)
			.stream()
			.filter(p -> p.scope == ParameterScope.GLOBAL)
			.sorted((p1, p2) -> Strings.compareIgnoreCase(p1.name, p2.name))
			.forEach(p -> {
				var list = p.isInputParameter
					? inputParams
					: calcParams;
				list.add(p);
			});

		sheet.next(Section.GLOBAL_INPUT_PARAMETERS);
		writeInputParams(sheet, inputParams);
		sheet.next();

		sheet.next(Section.GLOBAL_CALCULATED_PARAMETERS);
		writeDependentParams(sheet, calcParams);
		sheet.next();
	}

	private void writeInputParams(SheetWriter sheet, List<Parameter> params) {
		sheet.header(
			Field.NAME,
			Field.VALUE,
			Field.UNCERTAINTY,
			Field.MEAN_MODE,
			Field.SD,
			Field.MINIMUM,
			Field.MAXIMUM,
			Field.DESCRIPTION);
		for (var param : params) {
			sheet.next(row -> {
				row.next(param.name);
				row.next(param.value);
				row.next(param.uncertainty);
				row.next(param.description);
			});
		}
	}

	private void writeDependentParams(SheetWriter sheet, List<Parameter> params) {
		sheet.header(
			Field.NAME,
			Field.FORMULA,
			Field.DESCRIPTION);
		for (var param : params) {
			sheet.next(row -> {
				row.next(param.name);
				row.next(param.formula);
				row.next(param.description);
			});
		}
	}
}
