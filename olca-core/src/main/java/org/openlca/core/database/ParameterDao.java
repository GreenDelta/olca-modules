package org.openlca.core.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterType;

public class ParameterDao extends BaseDao<Parameter> {

	public ParameterDao(IDatabase database) {
		super(Parameter.class, database);
	}

	public List<Parameter> getAllForType(ParameterType type) throws Exception {
		String jpql = "select p from Parameter p where p.type = :type";
		Map<String, Object> args = new HashMap<>();
		args.put("type", type);
		return getAll(jpql, args);
	}

	public List<Parameter> getAllForName(String name, ParameterType type)
			throws Exception {
		String jpql = "select p from Parameter p where lower(p.name) = "
				+ "lower(:name) and p.type = :type";
		Map<String, Object> args = new HashMap<>();
		args.put("name", name);
		args.put("type", type);
		return getAll(jpql, args);
	}

}
