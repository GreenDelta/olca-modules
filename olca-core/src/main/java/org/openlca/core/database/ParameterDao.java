package org.openlca.core.database;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.openlca.core.model.Category;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.ParameterDescriptor;

public class ParameterDao extends
        RootEntityDao<Parameter, ParameterDescriptor> {

	public ParameterDao(IDatabase database) {
		super(Parameter.class, ParameterDescriptor.class, database);
	}

	@SuppressWarnings("unchecked")
	public List<Parameter> getParametersOf(long ownerId) {
		var sql = "select * from tbl_parameters "
				+ "where f_owner = " + ownerId;
		return (List<Parameter>) db.newEntityManager()
				.createNativeQuery(sql, Parameter.class)
				.getResultList();
	}
	
	public List<Parameter> getGlobalParameters() {
		var jpql = "select p from Parameter p where p.scope = :scope";
		var args = new HashMap<String, Object>();
		args.put("scope", ParameterScope.GLOBAL);
		return getAll(jpql, args);
	}

	public List<ParameterDescriptor> getGlobalDescriptors() {
		return queryDescriptors(
				"where d.scope = '" + ParameterScope.GLOBAL.name() + "'", List.of()) ;
	}

	@Override
	public List<ParameterDescriptor> getDescriptors(Optional<Category> category) {
		var cond = category.isPresent()
			? "where d.f_category = " + category.get().id
			: "where d.f_category is null";
		cond += " and d.scope = '" + ParameterScope.GLOBAL.name() + "'";
		return queryDescriptors(cond, List.of());
	}

	public boolean existsGlobal(String name) {
		if (name == null)
			return false;
		var jpql = "SELECT count(param) FROM Parameter param "
				+ "WHERE lower(param.name) = :name "
				+ "AND param.scope = :scope";
		var parameters = new HashMap<String, Object>();
		parameters.put("name", name.toLowerCase());
		parameters.put("scope", ParameterScope.GLOBAL);
		return getCount(jpql, parameters) > 0;
	}

}
