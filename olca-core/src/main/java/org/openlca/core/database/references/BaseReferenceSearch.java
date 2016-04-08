package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.references.Search.Ref;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ParameterDescriptor;
import org.openlca.util.Formula;

abstract class BaseReferenceSearch<T extends CategorizedDescriptor> implements
		IReferenceSearch<T> {

	protected final IDatabase database;
	private final Class<? extends CategorizedEntity> type;
	private final boolean includeOptional;

	BaseReferenceSearch(IDatabase database,
			Class<? extends CategorizedEntity> type) {
		this(database, type, false);
	}

	BaseReferenceSearch(IDatabase database,
			Class<? extends CategorizedEntity> type, boolean includeOptional) {
		this.database = database;
		this.type = type;
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
		return findReferences(table, idField, ids, null, references);
	}

	protected List<Reference> findReferences(String table, String idField,
			Set<Long> ids, Map<Long, Long> idToOwnerId, Ref[] references) {
		return Search.on(database, type).findReferences(table, idField, ids,
				idToOwnerId, references, includeOptional);
	}

	protected List<Reference> findGlobalParameters(Set<Long> ids,
			Map<Long, Set<String>> idToFormulas) {
		if (ids.size() == 0)
			return Collections.emptyList();
		Map<Long, Set<String>> idToNames = new HashMap<>();
		String idList = Search.asSqlList(ids.toArray());
		String paramQuery = "SELECT f_owner, name, is_input_param, formula FROM tbl_parameters "
				+ "WHERE f_owner IN (" + idList + ")";
		Search.on(database, type).query(paramQuery, (result) -> {
			long ownerId = result.getLong(1);
			Set<String> names = idToNames.get(ownerId);
			if (names == null)
				idToNames.put(ownerId, names = new HashSet<>());
			names.add(result.getString(2));
			if (!result.getBoolean(3)) {
				Set<String> formulas = idToFormulas.get(ownerId);
				if (formulas == null)
					idToFormulas.put(ownerId, formulas = new HashSet<>());
				formulas.add(result.getString(4));
			}
		});
		Map<Long, Set<String>> undeclared = findUndeclaredParameters(idToNames,
				idToFormulas);
		Set<String> names = new HashSet<>();
		for (Set<String> n : undeclared.values())
			names.addAll(n);
		List<ParameterDescriptor> descriptors = new ParameterDao(database)
				.getDescriptors(names.toArray(new String[names.size()]),
						ParameterScope.GLOBAL);
		List<Reference> results = toReferences(descriptors, false, undeclared,
				null);
		Set<String> found = new HashSet<>();
		for (ParameterDescriptor d : descriptors)
			found.add(d.getName());
		for (String name : names)
			if (!found.contains(name)) {
				Reference ref = createMissingReference(name, undeclared);
				if (ref != null)
					results.add(ref);
			}
		return results;
	}

	private Reference createMissingReference(String name,
			Map<Long, Set<String>> ownerToNames) {
		for (long ownerId : ownerToNames.keySet())
			if (ownerToNames.get(ownerId).contains(name))
				return new Reference("", Parameter.class, 0, type, ownerId);
		return null;
	}

	protected Map<Long, Set<String>> findUndeclaredParameters(
			Map<Long, Set<String>> idToNames,
			Map<Long, Set<String>> idToFormulas) {
		Map<Long, Set<String>> undeclared = new HashMap<>();
		for (long id : idToFormulas.keySet()) {
			Set<String> formulas = idToFormulas.get(id);
			for (String formula : formulas) {
				for (String var : Formula.getVariables(formula)) {
					Set<String> set = idToNames.get(id);
					if (set != null && set.contains(var))
						continue;
					Set<String> names = undeclared.get(id);
					if (names == null)
						undeclared.put(id, names = new HashSet<>());
					names.add(var);
				}
			}
		}
		return undeclared;
	}

	protected List<Reference> findGlobalParameterRedefs(Set<Long> ids) {
		return findGlobalParameterRedefs(ids, null);
	}

	protected List<Reference> findGlobalParameterRedefs(Set<Long> ids,
			Map<Long, Long> idToOwnerId) {
		if (ids.size() == 0)
			return Collections.emptyList();
		Map<Long, Set<String>> idToNames = new HashMap<>();
		String query = "SELECT f_owner, name FROM tbl_parameter_redefs "
				+ "WHERE (f_context is null OR f_context = 0) AND f_owner IN ("
				+ Search.asSqlList(ids.toArray()) + ")";
		Search.on(database, type).query(query, (result) -> {
			long ownerId = result.getLong(1);
			if (idToOwnerId != null)
				ownerId = idToOwnerId.get(ownerId);
			Set<String> names = idToNames.get(ownerId);
			if (names == null)
				idToNames.put(ownerId, names = new HashSet<>());
			names.add(result.getString(2));
		});
		Set<String> names = new HashSet<>();
		for (Set<String> n : idToNames.values())
			names.addAll(n);
		String[] nameArray = names.toArray(new String[names.size()]);
		List<ParameterDescriptor> descriptors = new ParameterDao(database)
				.getDescriptors(nameArray, ParameterScope.GLOBAL);
		return toReferences(descriptors, false, idToNames, "parameterRedefs");
	}

	protected <F extends AbstractEntity> List<Reference> filter(Class<F> clazz,
			List<Reference> references) {
		List<Reference> filtered = new ArrayList<>();
		for (Reference reference : references)
			if (clazz.isAssignableFrom(reference.getType()))
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

	protected Map<Long, Long> toIdMap(List<Reference> references) {
		Map<Long, Long> ids = new HashMap<>();
		for (Reference r : references)
			ids.put(r.id, r.ownerId);
		return ids;
	}

	protected List<Reference> toReferences(
			List<ParameterDescriptor> descriptors, boolean optional,
			Map<Long, Set<String>> idToNames, String property) {
		Map<Long, Set<Long>> descriptorToOwnerIds = new HashMap<>();
		for (ParameterDescriptor d : descriptors) {
			for (long ownerId : idToNames.keySet()) {
				if (!idToNames.get(ownerId).contains(d.getName()))
					continue;
				Set<Long> set = descriptorToOwnerIds.get(d.getId());
				if (set == null)
					descriptorToOwnerIds.put(d.getId(), set = new HashSet<>());
				set.add(ownerId);
			}
		}
		List<Reference> references = new ArrayList<>();
		for (BaseDescriptor descriptor : descriptors) {
			for (long ownerId : descriptorToOwnerIds.get(descriptor.getId())) {
				Class<? extends AbstractEntity> type = (Class<? extends AbstractEntity>) descriptor
						.getModelType().getModelClass();
				long id = descriptor.getId();
				Reference reference = new Reference(property, type, id,
						this.type, ownerId, optional);
				references.add(reference);
			}
		}
		return references;
	}
}
