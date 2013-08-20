package org.openlca.core.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;

public class ParameterDao extends BaseDao<Parameter> {

	public ParameterDao(IDatabase database) {
		super(Parameter.class, database);
	}

	public List<Parameter> getGlobalParameters() {
		String jpql = "select p from Parameter p where p.scope = :scope";
		Map<String, Object> args = new HashMap<>();
		args.put("scope", ParameterScope.GLOBAL);
		return getAll(jpql, args);
	}

}
