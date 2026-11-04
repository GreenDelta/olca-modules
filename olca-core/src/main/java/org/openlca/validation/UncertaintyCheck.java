package org.openlca.validation;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.UncertaintyType;

import gnu.trove.map.hash.TLongLongHashMap;

/// Checks the parameters of uncertainty distributions of exchanges,
/// parameters, impact factors, and parameter redefinitions. Invalid values are
/// reported as warnings because the number generators can handle them (e.g. a
/// geometric standard deviation of 0 results in a discrete value), but they
/// are not valid in terms of the respective distribution.
class UncertaintyCheck implements Runnable {

	private final Validation v;
	private final NativeSql sql;
	private boolean foundErrors = false;

	UncertaintyCheck(Validation v) {
		this.v = v;
		this.sql = NativeSql.on(v.db);
	}

	@Override
	public void run() {
		try {
			checkExchanges();
			checkImpactFactors();
			checkParameters();
			checkParameterRedefs();
			if (!foundErrors && !v.wasCanceled()) {
				v.ok("checked uncertainty distributions");
			}
		} catch (Exception e) {
			v.error("error in uncertainty validation", e);
		} finally {
			v.workerFinished();
		}
	}

	private void checkExchanges() {
		if (v.wasCanceled())
			return;
		var q = "select f_owner, " + uncertaintyColumns() + " from tbl_exchanges"
				+ " where distribution_type > 0";
		sql.query(q, r -> {
			var err = validate(r, 2);
			if (err != null) {
				v.warning(r.getLong(1), ModelType.PROCESS, "exchange " + err);
				foundErrors = true;
			}
			return !v.wasCanceled();
		});
	}

	private void checkImpactFactors() {
		if (v.wasCanceled())
			return;
		var q = "select f_impact_category, " + uncertaintyColumns()
				+ " from tbl_impact_factors where distribution_type > 0";
		sql.query(q, r -> {
			var err = validate(r, 2);
			if (err != null) {
				v.warning(r.getLong(1), ModelType.IMPACT_CATEGORY,
						"characterization factor " + err);
				foundErrors = true;
			}
			return !v.wasCanceled();
		});
	}

	private void checkParameters() {
		if (v.wasCanceled())
			return;
		var q = "select id, scope, f_owner, name, " + uncertaintyColumns()
				+ " from tbl_parameters where distribution_type > 0";
		sql.query(q, r -> {
			var err = validate(r, 5);
			if (err == null)
				return !v.wasCanceled();
			var scope = ParameterScope.fromString(r.getString(2));
			var message = "parameter '" + r.getString(4) + "' " + err;
			switch (scope) {
				case PROCESS -> v.warning(r.getLong(3), ModelType.PROCESS, message);
				case IMPACT -> v.warning(
						r.getLong(3), ModelType.IMPACT_CATEGORY, message);
				case null, default -> v.warning(
						r.getLong(1), ModelType.PARAMETER, message);
			}
			foundErrors = true;
			return !v.wasCanceled();
		});
	}

	private void checkParameterRedefs() {
		if (v.wasCanceled())
			return;

		// parameter redefinitions are owned by parameter sets of product
		// systems or by project variants; we map them to their root entities
		var systems = new TLongLongHashMap();
		sql.query("select id, f_product_system from tbl_parameter_redef_sets",
				r -> {
					systems.put(r.getLong(1), r.getLong(2));
					return true;
				});
		var projects = new TLongLongHashMap();
		sql.query("select id, f_project from tbl_project_variants", r -> {
			projects.put(r.getLong(1), r.getLong(2));
			return true;
		});

		var q = "select f_owner, name, " + uncertaintyColumns()
				+ " from tbl_parameter_redefs where distribution_type > 0";
		sql.query(q, r -> {
			var err = validate(r, 3);
			if (err == null)
				return !v.wasCanceled();
			long owner = r.getLong(1);
			var message = "parameter redefinition '" + r.getString(2) + "' " + err;
			if (systems.containsKey(owner)) {
				v.warning(systems.get(owner), ModelType.PRODUCT_SYSTEM, message);
			} else if (projects.containsKey(owner)) {
				v.warning(projects.get(owner), ModelType.PROJECT, message);
			} else {
				v.warning(message + " (unknown owner @" + owner + ")");
			}
			foundErrors = true;
			return !v.wasCanceled();
		});
	}

	private static String uncertaintyColumns() {
		return "distribution_type, parameter1_value, parameter2_value, "
				+ "parameter3_value";
	}

	/// Validates the uncertainty distribution where the columns of the
	/// distribution start at the given position in the result set: type,
	/// parameter 1, parameter 2, parameter 3. Returns an error message if the
	/// distribution is invalid, otherwise `null`.
	private static String validate(ResultSet r, int pos) throws SQLException {
		int typeIdx = r.getInt(pos);
		var types = UncertaintyType.values();
		if (typeIdx < 0 || typeIdx >= types.length)
			return "has an unknown uncertainty distribution type: " + typeIdx;
		var type = types[typeIdx];
		var p1 = doubleOf(r, pos + 1);
		var p2 = doubleOf(r, pos + 2);
		var p3 = doubleOf(r, pos + 3);
		return validate(type, p1, p2, p3);
	}

	static String validate(
			UncertaintyType type, Double p1, Double p2, Double p3) {
		if (type == null)
			return null;
		return switch (type) {
			case NONE -> null;
			case LOG_NORMAL -> {
				if (hasNonFinite(p1, p2))
					yield "has a log-normal distribution with missing or invalid"
							+ " parameters";
				yield p2 < 1
						? "has a log-normal distribution with a geometric standard"
						+ " deviation < 1: " + p2
						: null;
			}
			case NORMAL -> {
				if (hasNonFinite(p1, p2))
					yield "has a normal distribution with missing or invalid"
							+ " parameters";
				yield p2 < 0
						? "has a normal distribution with a standard deviation < 0: "
						+ p2
						: null;
			}
			case UNIFORM -> {
				if (hasNonFinite(p1, p2))
					yield "has a uniform distribution with missing or invalid"
							+ " parameters";
				yield p1 > p2
						? "has a uniform distribution with minimum > maximum: "
						+ p1 + " > " + p2
						: null;
			}
			case TRIANGLE -> {
				if (hasNonFinite(p1, p2, p3))
					yield "has a triangle distribution with missing or invalid"
							+ " parameters";
				yield p1 > p2 || p2 > p3
						? "has a triangle distribution where not minimum <= mode"
						+ " <= maximum: " + p1 + ", " + p2 + ", " + p3
						: null;
			}
		};
	}

	private static Double doubleOf(ResultSet r, int pos) throws SQLException {
		double d = r.getDouble(pos);
		return r.wasNull() ? null : d;
	}

	private static boolean hasNonFinite(Double... values) {
		for (var x : values) {
			if (x == null || !Double.isFinite(x))
				return true;
		}
		return false;
	}
}
