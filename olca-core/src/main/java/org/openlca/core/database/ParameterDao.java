package org.openlca.core.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Category;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.ParameterDescriptor;

import com.google.common.base.Optional;

public class ParameterDao extends CategorizedEntityDao<Parameter, ParameterDescriptor> {

	public ParameterDao(IDatabase database) {
		super(Parameter.class, ParameterDescriptor.class, database);
	}

	public List<Parameter> getGlobalParameters() {
		String jpql = "select p from Parameter p where p.scope = :scope";
		Map<String, Object> args = new HashMap<>();
		args.put("scope", ParameterScope.GLOBAL);
		return getAll(jpql, args);
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
		List<Object[]> results = selectAll(sql, getDescriptorFields(), parameters);
		return createDescriptors(results);
	}

}
