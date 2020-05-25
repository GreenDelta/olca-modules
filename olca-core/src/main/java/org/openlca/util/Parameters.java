package org.openlca.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.ParameterizedEntity;
import org.openlca.core.model.Process;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.InterpreterException;
import org.openlca.formula.Formulas;

public class Parameters {

	private Parameters() {
	}

	/**
	 * Returns true if the given name is a valid identifier for a parameter. We
	 * allow the same rules as for Java identifiers.
	 */
	public static boolean isValidName(String name) {
		if (name == null)
			return false;
		String id = name.trim();
		if (id.isEmpty())
			return false;
		for (int i = 0; i < id.length(); i++) {
			char c = id.charAt(i);
			if (i == 0 && !Character.isLetter(c))
				return false;
			if (i > 0 && !Character.isJavaIdentifierPart(c))
				return false;
		}

		// TODO: better if we would use the lexer rules here
		FormulaInterpreter interpreter = new FormulaInterpreter();
		interpreter.bind(name, "1");
		try {
			interpreter.eval(name);
		} catch (InterpreterException e) {
			return false;
		}
		return true;
	}

	/**
	 * Find the entities in the database where the parameter of the given
	 * owner is redefined. In the user interface this can be used as a check
	 * if a the renaming of a local parameter will change other entities
	 * (projects or product systems) where this parameter is redefined.
	 */
	public static List<CategorizedDescriptor> findRedefOwners(
			ParameterizedEntity owner, Parameter param, IDatabase db) {

		if (owner == null || param == null || db == null)
			return Collections.emptyList();

		var sql = "select f_owner, name, f_context from tbl_parameter_redefs";
		var redefOwners = new TLongHashSet();
		NativeSql.on(db).query(sql, r -> {
			if (!eq(param.name, r.getString(2)))
				return true;
			long contextID = r.getLong(3);
			if (owner.id != contextID)
				return true;
			redefOwners.add(r.getLong(1));
			return true;
		});

		// collect project and product system descriptors
		swapRedefOwners(db, redefOwners);
		var projects = new ProjectDao(db)
				.getDescriptors()
				.stream()
				.filter(d -> redefOwners.contains(d.id));
		var systems = new ProductSystemDao(db)
				.getDescriptors()
				.stream()
				.filter(d -> redefOwners.contains(d.id));
		return Stream
				.concat(projects, systems)
				.collect(Collectors.toList());
	}

	/**
	 * Rename the given parameter of the given owner. This will rename it in all formulas
	 * of the owner **and** in redefinitions of this parameter in projects and product
	 * systems. The updated owner will be returned.
	 */
	public static ParameterizedEntity rename(
			IDatabase db, ParameterizedEntity owner, Parameter param, String name) {
		if (owner instanceof Process) {
			var process = (Process) owner;
			for (var e : process.exchanges) {
				if (e.formula != null) {
					e.formula = Formulas.renameVariable(e.formula, param.name, name);
				}
			}
		}

		return null;
	}

	/**
	 * Renames the given global parameter in the database. Renaming the parameter
	 * means that it is also renamed in all places where it is used: formulas
	 * of exchanges, impact factors, other parameters, and parameter redefinitions.
	 * Formulas of which are in the scope of a local parameter with the same name
	 * are not changed.
	 */
	public static Parameter rename(IDatabase db, Parameter param, String name) {
		if (param.scope != ParameterScope.GLOBAL) {
			throw new IllegalArgumentException(
					param + " is not defined in the global scope");
		}
		if (!isValidName(name)) {
			throw new IllegalArgumentException(
					name + " is not a valid parameter name");
		}

		// if the parameter has no name or if it is equivalent to the
		// new name, we do not have to change the formulas or redefinitions
		if (Strings.nullOrEmpty(param.name) || eq(param.name, name)) {
			param.name = name;
			Version.incUpdate(param);
			param.lastChange = new Date().getTime();
			return new ParameterDao(db).update(param);
		}

		// collect the IDs of processes and impact categories where
		// a local parameter with the same name is defined
		var localOwners = new TLongHashSet();
		String sql = "select name, f_owner from tbl_parameters";
		NativeSql.on(db).query(sql, r -> {
			long owner = r.getLong(2);
			if (r.wasNull() || owner == 0)
				return true;
			String n = r.getString(1);
			if (eq(n, name) || eq(n, param.name)) {
				localOwners.add(owner);
			}
			return true;
		});

		var updatedOwners = new TLongHashSet();

		NativeSql.QueryResultHandler formulaUpdate = r -> {
			long owner = r.getLong(1);
			if (owner != 0 && localOwners.contains(owner))
				return true;
			String formula = r.getString(2);
			if (!hasVariable(formula, param.name))
				return true;
			formula = Formulas.renameVariable(
					formula, param.name, name);
			r.updateString(2, formula);
			r.updateRow();
			updatedOwners.add(owner);
			return true;
		};

		// rename unbound variables in parameter formulas
		sql = "select f_owner, formula from tbl_parameters" +
				" where formula is not null";
		NativeSql.on(db).updateRows(sql, formulaUpdate);

		// rename unbound variables in exchange formulas
		sql = "select f_owner, resulting_amount_formula from" +
				" tbl_exchanges where resulting_amount_formula is not null";
		NativeSql.on(db).updateRows(sql, formulaUpdate);

		// rename unbound variables in impact factor formulas
		sql = "select f_impact_category, formula from tbl_impact_factors" +
				" where formula is not null";
		NativeSql.on(db).updateRows(sql, formulaUpdate);

		// rename unbound variables in formulas of allocation factors
		sql = "select f_process, formula from tbl_allocation_factors" +
				" where formula is not null";
		NativeSql.on(db).updateRows(sql, formulaUpdate);

		// rename redefinitions of global parameters
		sql = "select f_owner, name, f_context from tbl_parameter_redefs";
		NativeSql.on(db).updateRows(sql, r -> {
			long context = r.getLong(3);
			if (context > 0L)
				return true;
			long owner = r.getLong(1);
			var n = r.getString(2);
			if (!eq(n, param.name))
				return true;
			r.updateString(2, name);
			r.updateRow();
			updatedOwners.add(owner);
			return true;
		});

		// update version numbers and last change dates
		// of the updated entities
		incVersions(updatedOwners, "tbl_processes", db);
		incVersions(updatedOwners, "tbl_impact_categories", db);

		// find product systems with updated parameter sets
		// and projects with updated variants
		swapRedefOwners(db, updatedOwners);
		incVersions(updatedOwners, "tbl_product_systems", db);
		incVersions(updatedOwners, "tbl_projects", db);

		db.clearCache();

		// finally, update the parameter
		param.name = name;
		Version.incUpdate(param);
		param.lastChange = new Date().getTime();
		return new ParameterDao(db).update(param);
	}

	/**
	 * Parameter redefinitions are used in inner objects (project variants
	 * or parameter sets) of root entities (projects or product systems).
	 * This utility function replaces the IDs of these inner objects with
	 * the IDs of the corresponding root entities in the given set of IDs.
	 */
	private static void swapRedefOwners(IDatabase db, TLongHashSet owners) {
		if (owners.isEmpty())
			return;

		NativeSql.QueryResultHandler swapOwner = (r) -> {
			long i = r.getLong(2);
			if (owners.contains(i)) {
				owners.remove(i);
				owners.add(r.getLong(1));
			}
			return true;
		};

		// swap parameter set IDs => product system IDs
		var sql = "select sys.id as sysid, params.id as paramid" +
				" from tbl_product_systems sys inner join" +
				" tbl_parameter_redef_sets params on" +
				" params.f_product_system = sys.id";
		NativeSql.on(db).query(sql, swapOwner);

		// swap project variant IDs => project IDs
		sql = "select proj.id as projid, var.id as varid" +
				" from tbl_projects proj inner join" +
				" tbl_project_variants var on" +
				" var.f_project = proj.id";
		NativeSql.on(db).query(sql, swapOwner);
	}

	/**
	 * Returns true if both parameter names are equivalent regarding the formula
	 * interpreter.
	 */
	private static boolean eq(String name1, String name2) {
		if (name1 == null || name2 == null)
			return false;
		return Strings.nullOrEqual(
				name1.trim().toLowerCase(),
				name2.trim().toLowerCase());
	}

	private static boolean hasVariable(String formula, String variable) {
		if (formula == null || variable == null)
			return false;
		return Formulas.getVariables(formula)
				.stream()
				.anyMatch(v -> eq(v, variable));
	}

	/**
	 * Increment the versions and last change dates of the entities in the
	 * given table with an ID of the given ID set.
	 */
	private static void incVersions(TLongHashSet ids, String table, IDatabase db) {
		if (ids.isEmpty())
			return;
		String sql = "select id, version, last_change from " + table;
		long date = new Date().getTime();
		NativeSql.on(db).updateRows(sql, r -> {
			long id = r.getLong(1);
			if (!ids.contains(id))
				return true;
			var v = new Version(r.getLong(2));
			v.incUpdate();
			r.updateLong(2, v.getValue());
			r.updateLong(3, date);
			r.updateRow();
			return true;
		});
	}
}
