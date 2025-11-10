package org.openlca.io.xls.process;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;

class InParameterSync {

	private final InConfig config;
	private final Process process;

	private InParameterSync(InConfig config) {
		this.config = config;
		this.process = config.process();
		process.parameters.clear();
	}

	static void sync(InConfig wb) {
		new InParameterSync(wb).read();
	}

	private void read() {
		var sheet = config.getSheet(Tab.PARAMETERS);
		if (sheet == null)
			return;
		syncGlobals(sheet);
		sheet.eachRow(Section.INPUT_PARAMETERS,
			row -> process.parameters.add(
				readInputParam(row, ParameterScope.PROCESS)));
		sheet.eachRow(Section.CALCULATED_PARAMETERS,
			row -> process.parameters.add(
				readDependentParam(row, ParameterScope.PROCESS)));
	}

	private void syncGlobals(SheetReader sheet) {

		Function<String, String> key = s -> s != null
			? s.strip().toLowerCase()
			: "";

		var globals = new HashMap<String, Parameter>();
		config.db().getAll(Parameter.class).stream()
			.filter(p -> p.scope == ParameterScope.GLOBAL
				&& p.name != null)
			.forEach(p -> globals.put(key.apply(p.name), p));

		var globalSections = List.of(
			Section.GLOBAL_INPUT_PARAMETERS,
			Section.GLOBAL_CALCULATED_PARAMETERS);
		for (var section : globalSections) {
			sheet.eachRow(section, row -> {
				var pkey = key.apply(row.str(Field.NAME));
				if (globals.containsKey(pkey))
					return;
				var param = section == Section.GLOBAL_INPUT_PARAMETERS
					? readInputParam(row, ParameterScope.GLOBAL)
					: readDependentParam(row, ParameterScope.GLOBAL);
				param = config.db().insert(param);
				globals.put(pkey, param);
			});
		}
	}

	private Parameter readDependentParam(RowReader row, ParameterScope scope) {
		var p = new Parameter();
		p.name = row.str(Field.NAME);
		p.isInputParameter = false;
		p.scope = scope;
		p.formula = row.str(Field.FORMULA);
		p.description = row.str(Field.DESCRIPTION);
		return p;
	}

	private Parameter readInputParam(RowReader row, ParameterScope scope) {
		var p = new Parameter();
		p.name = row.str(Field.NAME);
		p.isInputParameter = true;
		p.scope = scope;
		p.value = row.num(Field.VALUE);
		p.uncertainty = row.uncertainty();
		p.description = row.str(Field.DESCRIPTION);
		return p;
	}
}
