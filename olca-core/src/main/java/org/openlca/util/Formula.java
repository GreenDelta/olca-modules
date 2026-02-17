package org.openlca.util;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openlca.commons.Strings;
import org.openlca.expressions.FormulaParser;
import org.openlca.expressions.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Formula {

	public static boolean matches(String formula, String parameterName) {
		if (formula == null)
			return false;
		var f = formula.trim();
		if (f.equalsIgnoreCase(parameterName))
			return true;
		try {
			var vars = Formula.getVariables(f);
			for (var var : vars) {
				if (var.equalsIgnoreCase(parameterName))
					return true;
			}
		} catch (Error e) {
			return false;
		}
		return false;
	}
	
	public static Set<String> getVariables(String formula) {
		if (Strings.isBlank(formula))
			return Collections.emptySet();
		var vars = new HashSet<String>();
		try {
			var reader = new StringReader(formula.toLowerCase());
			var parser = new FormulaParser(reader);
			parser.parse();
			for (var f : parser.getVariables()) {
				vars.add(f.getVariableName());
			}
			return vars;
		} catch (ParseException e) {
			Logger log = LoggerFactory.getLogger(Formula.class);
			log.warn("Failed to parse formula " + formula, e);
			return Collections.emptySet();
		}
	}

}
