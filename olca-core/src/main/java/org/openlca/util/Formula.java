package org.openlca.util;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openlca.expressions.FormulaParser;
import org.openlca.expressions.ParseException;
import org.openlca.expressions.VariableFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Formula {

	public static Set<String> getVariables(String formula) {
		if (Strings.nullOrEmpty(formula))
			return Collections.emptySet();
		Set<String> vars = new HashSet<>();
		try {
			Reader reader = new StringReader(formula.toLowerCase());
			FormulaParser parser = new FormulaParser(reader);
			parser.parse();
			for (VariableFunction f : parser.getVariables()) {
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
