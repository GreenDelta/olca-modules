package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.references.Search.Ref;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ParameterDescriptor;
import org.openlca.util.Formula;

abstract class BaseReferenceSearch<T extends CategorizedDescriptor> implements
		IReferenceSearch<T> {

	protected final IDatabase database;
	private final boolean includeOptional;

	BaseReferenceSearch(IDatabase database) {
		this(database, false);
	}

	BaseReferenceSearch(IDatabase database, boolean includeOptional) {
		this.database = database;
		this.includeOptional = includeOptional;
	}

	@Override
	public List<Reference> findReferences(T descriptor) {
		if (descriptor == null || descriptor.getId() == 0l)
			return Collections.emptyList();
		return findReferences(Collections.singletonList(descriptor));
	}

	@Override
	public List<Reference> findReferences(List<T> descriptors) {
		if (descriptors == null || descriptors.isEmpty())
			return Collections.emptyList();
		return findReferences(toIdSet(descriptors));
	}

	@Override
	public List<Reference> findReferences(long id) {
		if (id == 0l)
			return Collections.emptyList();
		return findReferences(Collections.singleton(id));
	}

	protected List<Reference> findReferences(String table, String idField,
			Set<Long> ids, Ref[] references) {
		return Search.on(database).findReferences(table, idField, ids,
				references, includeOptional);
	}

	protected List<Reference> findGlobalParameters(Set<Long> ids,
			Set<String> formulas) {
		if (ids.size() == 0)
			return Collections.emptyList();
		String idList = Search.asSqlList(ids.toArray());
		String paramQuery = "SELECT name, is_input_param, formula FROM tbl_parameters "
				+ "WHERE f_owner IN (" + idList + ")";
		Set<String> names = new HashSet<>();
		Search.on(database).query(paramQuery, (result) -> {
			names.add(result.getString(1));
			if (!result.getBoolean(2))
				formulas.add(result.getString(3));
		});
		String[] global = findUndeclaredParameters(names, formulas);
		List<ParameterDescriptor> descriptors = new ParameterDao(database)
				.getDescriptors(global, ParameterScope.GLOBAL);
		return toReferences(descriptors, false);
	}

	private String[] findUndeclaredParameters(Set<String> declared,
			Set<String> formulas) {
		Set<String> variables = new HashSet<>();
		for (String formula : formulas)
			variables.addAll(Formula.getVariables(formula));
		List<String> globalNames = new ArrayList<>();
		for (String variable : variables)
			if (!declared.contains(variable))
				globalNames.add(variable);
		return globalNames.toArray(new String[globalNames.size()]);
	}

	protected List<Reference> findGlobalParameterRedefs(Set<Long> ids) {
		if (ids.size() == 0)
			return Collections.emptyList();
		String query = "SELECT name FROM tbl_parameter_redefs "
				+ "WHERE f_context is null OR f_context = 0";
		Set<String> names = new HashSet<>();
		Search.on(database).query(query, (result) -> {
			names.add(result.getString(1));
		});
		String[] nameArray = names.toArray(new String[names.size()]);
		List<ParameterDescriptor> descriptors = new ParameterDao(database)
				.getDescriptors(nameArray, ParameterScope.GLOBAL);
		return toReferences(descriptors, false);
	}

	protected <F extends AbstractEntity> List<Reference> filter(Class<F> clazz,
			List<Reference> references) {
		List<Reference> filtered = new ArrayList<>();
		for (Reference reference : references)
			if (clazz.isAssignableFrom(reference.type))
				filtered.add(reference);
		return filtered;
	}

	protected Set<Long> toIdSet(List<?> objects) {
		Set<Long> ids = new HashSet<>();
		for (Object o : objects)
			if (o instanceof Reference)
				ids.add(((Reference) o).id);
			else if (o instanceof BaseDescriptor)
				ids.add(((BaseDescriptor) o).getId());
		return ids;
	}

	@SuppressWarnings("unchecked")
	protected List<Reference> toReferences(
			List<? extends BaseDescriptor> descriptors, boolean optional) {
		List<Reference> references = new ArrayList<>();
		for (BaseDescriptor descriptor : descriptors) {
			Class<? extends AbstractEntity> type = (Class<? extends AbstractEntity>) descriptor
					.getModelType().getModelClass();
			long id = descriptor.getId();
			Reference reference = new Reference(type, id, optional);
			references.add(reference);
		}
		return references;
	}

}
