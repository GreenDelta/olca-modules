package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Category;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.ParameterDescriptor;
import org.openlca.util.Strings;

import com.google.common.base.Optional;

public class ParameterDao extends
		CategorizedEntityDao<Parameter, ParameterDescriptor> {

	public ParameterDao(IDatabase database) {
		super(Parameter.class, ParameterDescriptor.class, database);
	}

	public List<Parameter> getGlobalParameters() {
		String jpql = "select p from Parameter p where p.scope = :scope";
		Map<String, Object> args = new HashMap<>();
		args.put("scope", ParameterScope.GLOBAL);
		return getAll(jpql, args);
	}

	public List<ParameterDescriptor> getGlobalDescriptors() {
		String sql = getDescriptorQuery();
		List<Object> parameters = new ArrayList<>();
		sql += " where scope = ?";
		parameters.add(ParameterScope.GLOBAL.name());
		List<Object[]> results = selectAll(sql, getDescriptorFields(),
				parameters);
		return createDescriptors(results);
	}

	@Override
	public List<ParameterDescriptor> getDescriptors(Optional<Category> category) {
		String sql = getDescriptorQuery();
		List<Object> parameters = new ArrayList<>();
		if (category.isPresent()) {
			sql += " where f_category = ?";
			parameters.add(category.get().getId());
		} else {
			sql += " where f_category is null";
		}
		sql += " and scope = ?";
		parameters.add(ParameterScope.GLOBAL.name());
		List<Object[]> results = selectAll(sql, getDescriptorFields(),
				parameters);
		return createDescriptors(results);
	}

	public List<ParameterDescriptor> getDescriptors(String[] names) {
		return getDescriptors(names, null);
	}

	public List<ParameterDescriptor> getDescriptors(String[] names,
			ParameterScope scope) {
		if (names == null || names.length == 0)
			return Collections.emptyList();
		StringBuilder sql = new StringBuilder(getDescriptorQuery());
		List<Object> parameters = new ArrayList<>();
		String[] list = new String[names.length];
		for (int i = 0; i < names.length; i++)
			list[i] = "'" + names[i].toLowerCase() + "'";
		sql.append(" WHERE lower(name) IN (" + Strings.join(list, ',') + ")");
		if (scope != null) {
			sql.append(" AND scope = ?");
			parameters.add(scope.name());
		}
		List<Object[]> results = selectAll(sql.toString(),
				getDescriptorFields(), parameters);
		return createDescriptors(results);
	}

	public boolean existsGlobal(String name) {
		if (name == null)
			return false;
		String jpql = "SELECT count(param) FROM Parameter param "
				+ "WHERE lower(param.name) = :name "
				+ "AND param.scope = :scope";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("name", name.toLowerCase());
		parameters.put("scope", ParameterScope.GLOBAL);
		return getCount(jpql, parameters) > 0;
	}

}
