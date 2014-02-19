package org.openlca.core.matrix;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.UncertaintyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

class ParameterTableBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Set<Long> contexts;

	ParameterTable build(IDatabase database, Set<Long> contexts) {
		this.contexts = contexts;
		final ParameterTable table = new ParameterTable();
		try {
			putParameters(database, table);
		} catch (Exception e) {
			log.error("error while building parameter table", e);
		}
		this.contexts = null;
		return table;
	}

	private void putParameters(IDatabase database, final ParameterTable table)
			throws Exception {
		String query = "select * from tbl_parameters";
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				CalcParameter param = makeParam(result);
				if (param != null)
					table.put(param);
				return true;
			}
		});
	}

	private CalcParameter makeParam(ResultSet r) {
		try {
			ParameterScope scope = ParameterScope.valueOf(r
					.getString("scope"));
			long owner = r.getLong("f_owner");
			if (scope != ParameterScope.GLOBAL && !contexts.contains(owner))
				return null;
			if(scope == ParameterScope.GLOBAL)
				owner = 0;
			CalcParameter param = new CalcParameter();
			param.setName(r.getString("name"));
			param.setInputParameter(r.getBoolean("is_input_param"));
			param.setOwner(owner);
			param.setScope(scope);
			param.setValue(r.getDouble("value"));
			param.setFormula(r.getString("formula"));
			addUncertaintyInfo(r, param);
			return param;
		} catch (Exception e) {
			log.error("failed to get parameter values from db", e);
			return null;
		}
	}

	private void addUncertaintyInfo(ResultSet r, CalcParameter param)
			throws SQLException {
		int uncertaintyType = r.getInt("distribution_type");
		if (!r.wasNull()) {
			param.setUncertaintyType(UncertaintyType.values()[uncertaintyType]);
			param.setParameter1(r.getDouble("parameter1_value"));
			param.setParameter2(r.getDouble("parameter2_value"));
			param.setParameter3(r.getDouble("parameter3_value"));
			param.setParameter1Formula(r.getString("parameter1_formula"));
			param.setParameter2Formula(r.getString("parameter2_formula"));
			param.setParameter3Formula(r.getString("parameter3_formula"));
		}
	}
}
