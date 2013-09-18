package org.openlca.core.matrix;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FormulaInterpreterBuilder {

	private FormulaInterpreterBuilder() {
	}

	/**
	 * Applies the given parameter redefinitions to the bindings of the given
	 * interpreter.
	 */
	public static void apply(List<ParameterRedef> redefs,
			FormulaInterpreter interpreter) {
		if (redefs == null || interpreter == null)
			return;
		for (ParameterRedef redef : redefs) {
			Scope scope = findScope(redef, interpreter);
			if (scope == null)
				continue;
			scope.bind(redef.getName(), Double.toString(redef.getValue()));
		}
	}

	private static Scope findScope(ParameterRedef redef,
			FormulaInterpreter interpreter) {
		if (redef.getProcessId() == null)
			return interpreter.getGlobalScope();
		else
			return interpreter.getScope(redef.getProcessId());
	}

	/**
	 * Creates a new formula interpreter. The global parameters are bound to the
	 * global scope of the interpreter. For each process a local scope is
	 * created if the process is in the given list and if the process has
	 * parameters.
	 */
	public static FormulaInterpreter build(IDatabase database,
			Set<Long> processIds) {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		try (Connection con = database.createConnection()) {
			String query = "select * from tbl_parameters";
			Statement stmt = con.createStatement();
			ResultSet results = stmt.executeQuery(query);
			while (results.next()) {
				String name = results.getString("name");
				String formula = results.getString("formula");
				double value = results.getDouble("value");
				ParameterScope scopeType = ParameterScope.valueOf(results
						.getString("scope"));
				long owner = results.getLong("f_owner");
				Scope scope = findScope(interpreter, scopeType, owner,
						processIds);
				if (scope == null)
					continue;
				bindValue(name, formula, value, scope);
			}
			results.close();
			stmt.close();
		} catch (Exception e) {
			Logger log = LoggerFactory
					.getLogger(FormulaInterpreterBuilder.class);
			log.error("Failed to build formula interpreter", e);
		}
		return interpreter;
	}

	private static Scope findScope(FormulaInterpreter interpreter,
			ParameterScope scopeType, long owner, Set<Long> processIds) {
		if (scopeType == ParameterScope.GLOBAL)
			return interpreter.getGlobalScope();
		if (!processIds.contains(owner))
			return null;
		Scope scope = interpreter.getScope(owner);
		if (scope == null)
			scope = interpreter.createScope(owner);
		return scope;
	}

	private static void bindValue(String name, String formula, double value,
			Scope scope) {
		if (formula == null)
			scope.bind(name, Double.toString(value));
		else
			scope.bind(name, formula);
	}
}
