package org.openlca.util;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.openlca.expressions.FormulaParser;
import org.openlca.expressions.ParseException;
import org.openlca.expressions.VariableFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Formula {
	
	private static final Logger log = LoggerFactory.getLogger(Formula.class);
	
	public static Set<String> getVariables(String formula) {
		Set<String> params = new HashSet<>();
		if (formula == null || formula.isEmpty())
			return params;
		Reader reader = new StringReader(formula.toLowerCase());
		FormulaParser parser = new FormulaParser(reader);
		try {
			parser.parse();
		} catch (ParseException e) {
			log.warn("unexpected error in formula parsing", e);
		}
		for (VariableFunction f : parser.getVariables())
			params.add(f.getVariableName());
		return params;
	}

}
