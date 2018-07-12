package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ParameterDescriptor;
import org.openlca.util.Formula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BaseParametrizedReferenceSearch<T extends CategorizedDescriptor> extends BaseReferenceSearch<T> {

	private final static Logger log = LoggerFactory.getLogger(BaseReferenceSearch.class);

	BaseParametrizedReferenceSearch(IDatabase database, Class<? extends CategorizedEntity> type, boolean includeOptional) {
		super(database, type, includeOptional);
	}

	protected List<Reference> findParameters(Set<Long> ids, Map<Long, Set<String>> formulas) {
		Map<Long, Set<String>> names = new HashMap<>();
		List<String> queries = Search.createQueries(
				"SELECT f_owner, lower(name), is_input_param, lower(formula) FROM tbl_parameters",
				"WHERE f_owner IN", ids);
		collect(queries, names, formulas);
		Map<Long, Set<String>> undeclared = findUndeclaredParameters(names, formulas);
		Set<String> allUndeclared = merge(undeclared);
		List<ParameterDescriptor> descriptors = new ParameterDao(database).getDescriptors(
				allUndeclared.toArray(new String[allUndeclared.size()]), ParameterScope.GLOBAL);
		List<Reference> results = toReferences(descriptors, false, undeclared);
		Set<String> found = new HashSet<>();
		for (ParameterDescriptor d : descriptors) {
			found.add(d.getName().toLowerCase());
		}
		for (String name : allUndeclared) {
			if (found.contains(name))
				continue;
			results.addAll(createMissingParameterReferences(name, undeclared));
		}
		return results;
	}

	private Map<Long, Set<String>> findUndeclaredParameters(Map<Long, Set<String>> names,
			Map<Long, Set<String>> formulas) {
		Map<Long, Set<String>> undeclared = new HashMap<>();
		for (long id : formulas.keySet()) {
			for (String formula : formulas.get(id)) {
				try {
					for (String var : Formula.getVariables(formula)) {
						Set<String> set = names.get(id);
						if (set != null && set.contains(var))
							continue;
						put(id, var, undeclared);
					}
				} catch (Throwable e) {
					log.warn("Failed parsing formula " + formula + " in model " + id, e);
				}
			}
		}
		return undeclared;
	}

	private List<Reference> createMissingParameterReferences(String name, Map<Long, Set<String>> idToNames) {
		List<Reference> missing = new ArrayList<>();
		for (long ownerId : idToNames.keySet()) {
			if (idToNames.get(ownerId).contains(name.toLowerCase())) {
				missing.add(new Reference(name, Parameter.class, 0, type, ownerId));
			}
		}
		return missing;
	}

	protected List<Reference> findParameterRedefs(Set<Long> ids) {
		return findParameterRedefs(ids, null, null, null);
	}

	protected List<Reference> findParameterRedefs(Set<Long> ids, Map<Long, Long> idToOwnerId,
			Class<? extends AbstractEntity> nestedOwnerType, String nestedProperty) {
		Map<Long, Set<String>> names = new HashMap<>();
		List<String> queries = Search.createQueries(
				"SELECT f_owner, lower(name) FROM tbl_parameter_redefs WHERE context_type IS NULL",
				"AND f_owner IN", ids);
		collect(queries, names, null);
		Set<String> allNames = merge(names);
		List<ParameterDescriptor> descriptors = new ParameterDao(database).getDescriptors(
				allNames.toArray(new String[allNames.size()]), ParameterScope.GLOBAL);
		List<Reference> references = toReferences(descriptors, false, names, idToOwnerId, nestedOwnerType,
				nestedProperty);
		references.addAll(createMissingParameterRedefReferences(names, descriptors, idToOwnerId, nestedProperty, nestedOwnerType));
		return references;
	}

	private List<Reference> createMissingParameterRedefReferences(Map<Long, Set<String>> names,
			List<ParameterDescriptor> descriptors, Map<Long, Long> idToOwnerId, String nestedProperty,
			Class<? extends AbstractEntity> nestedOwnerType) {
		List<Reference> references = new ArrayList<>();
		for (long ownerId : names.keySet()) {
			namesLoop: for (String name : names.get(ownerId)) {
				for (ParameterDescriptor d : descriptors) {
					if (d.getName().equals(name)) {
						continue namesLoop;
					}
				}
				long nestedOwnerId = 0;
				if (idToOwnerId != null) {
					nestedOwnerId = ownerId;
					ownerId = idToOwnerId.get(ownerId);
				}
				references.add(new Reference(name, ParameterRedef.class, 0, type, ownerId, nestedProperty,
						nestedOwnerType, nestedOwnerId, false));
			}
		}
		return references;
	}

	protected List<Reference> toReferences(List<ParameterDescriptor> descriptors, boolean optional,
			Map<Long, Set<String>> names) {
		return toReferences(descriptors, optional, names, null, null, null);
	}

	private List<Reference> toReferences(List<ParameterDescriptor> descriptors, boolean optional,
			Map<Long, Set<String>> names, Map<Long, Long> ownerIds,
			Class<? extends AbstractEntity> nestedType, String nestedProperty) {
		Map<Long, Set<Long>> descriptorToOwnerIds = new HashMap<>();
		for (ParameterDescriptor d : descriptors) {
			for (long ownerId : names.keySet()) {
				if (!names.get(ownerId).contains(d.getName().toLowerCase()))
					continue;
				put(d.getId(), ownerId, descriptorToOwnerIds);
			}
		}
		List<Reference> references = new ArrayList<>();
		for (ParameterDescriptor descriptor : descriptors) {
			Set<Long> set = descriptorToOwnerIds.get(descriptor.getId());
			if (set == null)
				continue;
			for (long ownerId : set) {
				Class<? extends AbstractEntity> type = (Class<? extends AbstractEntity>)
						descriptor.getModelType().getModelClass();
				long id = descriptor.getId();
				long nestedOwnerId = 0;
				if (ownerIds != null) {
					nestedOwnerId = ownerId;
					ownerId = ownerIds.get(ownerId);
				}
				Reference reference = new Reference(descriptor.getName(), type, id, this.type, ownerId,
						nestedProperty, nestedType, nestedOwnerId, optional);
				references.add(reference);
			}
		}
		return references;
	}

	private void collect(List<String> queries, Map<Long, Set<String>> names, Map<Long, Set<String>> formulas) {
		for (String query : queries) {
			Search.on(database, type).query(query, (result) -> {
				long ownerId = result.getLong(1);
				put(ownerId, result.getString(2), names);
				if (formulas != null && !result.getBoolean(3)) {
					put(ownerId, result.getString(4), formulas);
				}
			});
		}
	}

	protected <V> void put(long key, V value, Map<Long, Set<V>> map) {
		Set<V> values = map.get(key);
		if (values == null) {
			map.put(key, values = new HashSet<>());
		}
		values.add(value);
	}

	private Set<String> merge(Map<Long, Set<String>> map) {
		Set<String> all = new HashSet<>();
		for (Set<String> n : map.values()) {
			all.addAll(n);
		}
		return all;
	}

}
