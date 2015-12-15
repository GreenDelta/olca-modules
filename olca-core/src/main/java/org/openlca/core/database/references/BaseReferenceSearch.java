package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.references.Search.Reference;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ParameterDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;
import org.openlca.util.Formula;

abstract class BaseReferenceSearch<T extends CategorizedDescriptor> implements
		IReferenceSearch<T> {

	private final static Reference[] factorReferences = { new Reference(
			ModelType.FLOW_PROPERTY, "f_flow_property") };
	private final static Reference[] unitReferences = { new Reference(
			ModelType.UNIT_GROUP, "f_unit_group") };

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
	public List<CategorizedDescriptor> findReferences(T descriptor) {
		if (descriptor == null || descriptor.getId() == 0l)
			return Collections.emptyList();
		return findReferences(Collections.singletonList(descriptor));
	}

	@Override
	public List<CategorizedDescriptor> findReferences(List<T> descriptors) {
		if (descriptors == null || descriptors.isEmpty())
			return Collections.emptyList();
		return findReferences(toIdSet(descriptors));
	}

	@Override
	public List<CategorizedDescriptor> findReferences(long id) {
		if (id == 0l)
			return Collections.emptyList();
		return findReferences(Collections.singleton(id));
	}

	protected List<CategorizedDescriptor> findReferences(String table,
			String idField, Set<Long> ids, Reference[] references) {
		List<BaseDescriptor> descriptors = findMixedReferences(table, idField,
				ids, references);
		return filterCategorized(descriptors);
	}

	protected List<BaseDescriptor> findMixedReferences(String table,
			String idField, Set<Long> ids, Reference[] references) {
		return Search.on(database).findMixedReferences(table, idField, ids,
				references, includeOptional);
	}

	protected List<CategorizedDescriptor> findFlowProperties(
			List<BaseDescriptor> factors) {
		Set<Long> factorIds = toIdSet(factors);
		return findReferences("tbl_flow_property_factors", "id", factorIds,
				factorReferences);
	}

	protected List<CategorizedDescriptor> findUnitGroups(
			List<UnitDescriptor> units) {
		Set<Long> unitIds = toIdSet(units);
		return findReferences("tbl_units", "id", unitIds, unitReferences);
	}

	protected List<ParameterDescriptor> findGlobalParameters(Set<Long> ids,
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
		return new ParameterDao(database).getDescriptors(global,
				ParameterScope.GLOBAL);
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

	protected List<ParameterDescriptor> findGlobalParameterRedefs(Set<Long> ids) {
		if (ids.size() == 0)
			return Collections.emptyList();
		String query = "SELECT name FROM tbl_parameter_redefs "
				+ "WHERE f_context is null OR f_context = 0";
		Set<String> names = new HashSet<>();
		Search.on(database).query(query, (result) -> {
			names.add(result.getString(1));
		});
		String[] nameArray = names.toArray(new String[names.size()]);
		return new ParameterDao(database).getDescriptors(nameArray,
				ParameterScope.GLOBAL);
	}

	protected List<CategorizedDescriptor> filterCategorized(
			List<BaseDescriptor> descriptors) {
		return filter(CategorizedDescriptor.class, descriptors);
	}

	protected List<UnitDescriptor> filterUnits(List<BaseDescriptor> descriptors) {
		return filter(UnitDescriptor.class, descriptors);
	}

	protected List<ImpactCategoryDescriptor> filterImpactCategories(
			List<BaseDescriptor> descriptors) {
		return filter(ImpactCategoryDescriptor.class, descriptors);
	}

	@SuppressWarnings("unchecked")
	private <F extends BaseDescriptor> List<F> filter(Class<F> clazz,
			List<BaseDescriptor> descriptors) {
		List<F> filtered = new ArrayList<>();
		for (BaseDescriptor descriptor : descriptors)
			if (clazz.isAssignableFrom(descriptor.getClass()))
				filtered.add((F) descriptor);
		return filtered;
	}

	protected List<BaseDescriptor> filterUnknown(
			List<BaseDescriptor> descriptors) {
		List<BaseDescriptor> filtered = new ArrayList<>();
		for (BaseDescriptor descriptor : descriptors)
			if (descriptor.getClass() == BaseDescriptor.class)
				filtered.add(descriptor);
		return filtered;
	}

	protected Set<Long> toIdSet(List<? extends BaseDescriptor> descriptors) {
		Set<Long> ids = new HashSet<>();
		for (BaseDescriptor descriptor : descriptors)
			ids.add(descriptor.getId());
		return ids;
	}

}
