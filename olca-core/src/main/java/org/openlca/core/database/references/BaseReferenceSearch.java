package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

abstract class BaseReferenceSearch<T extends CategorizedDescriptor> implements
		IReferenceSearch<T> {

	protected final IDatabase database;
	protected final Class<? extends CategorizedEntity> type;
	private final boolean includeOptional;

	BaseReferenceSearch(IDatabase database, Class<? extends CategorizedEntity> type) {
		this(database, type, false);
	}

	BaseReferenceSearch(IDatabase database, Class<? extends CategorizedEntity> type, boolean includeOptional) {
		this.database = database;
		this.type = type;
		this.includeOptional = includeOptional;
	}

	@Override
	public List<Reference> findReferences() {
		return findReferences(new HashSet<Long>());
	}

	@Override
	public List<Reference> findReferences(T descriptor) {
		if (descriptor == null || descriptor.id == 0l)
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

	protected List<Reference> findReferences(String table, String idField, Set<Long> ids, Ref[] references) {
		return findReferences(table, idField, ids, null, references);
	}

	protected List<Reference> findReferences(String table, String idField,
			Set<Long> ids, Map<Long, Long> idToOwnerId, Ref[] references) {
		return Search.on(database, type).findReferences(table, idField, ids, idToOwnerId, references, includeOptional);
	}

	protected <F extends AbstractEntity> List<Reference> filter(Class<F> clazz, List<Reference> references) {
		List<Reference> filtered = new ArrayList<>();
		for (Reference reference : references)
			if (clazz.isAssignableFrom(reference.getType()))
				filtered.add(reference);
		return filtered;
	}

	protected Set<Long> toIdSet(List<?> objects) {
		Set<Long> ids = new HashSet<>();
		for (Object o : objects) {
			if (o instanceof Reference) {
				ids.add(((Reference) o).id); 
			} else if (o instanceof Descriptor) {
				ids.add(((Descriptor) o).id);
			}
		}
		return ids;
	}

	protected Map<Long, Long> toIdMap(List<Reference> references) {
		Map<Long, Long> ids = new HashMap<>();
		for (Reference r : references) {
			ids.put(r.id, r.ownerId);
		}
		return ids;
	}

}
