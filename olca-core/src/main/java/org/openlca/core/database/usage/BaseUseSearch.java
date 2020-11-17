package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

abstract class BaseUseSearch<T extends CategorizedDescriptor> implements
		IUseSearch<T> {

	protected final IDatabase db;

	BaseUseSearch(IDatabase db) {
		this.db = db;
	}

	@Override
	public List<CategorizedDescriptor> findUses(T descriptor) {
		if (descriptor == null || descriptor.id == 0L)
			return Collections.emptyList();
		return findUses(Collections.singletonList(descriptor));
	}

	@Override
	public List<CategorizedDescriptor> findUses(List<T> descriptors) {
		if (descriptors == null || descriptors.isEmpty())
			return Collections.emptyList();
		return findUses(toIdSet(descriptors));
	}

	@Override
	public List<CategorizedDescriptor> findUses(long id) {
		if (id == 0L)
			return Collections.emptyList();
		return findUses(Collections.singleton(id));
	}

	protected List<CategorizedDescriptor> queryFor(ModelType type,
			Set<Long> toFind, String... inFields) {
		return Search.on(db).queryFor(type, toFind, inFields);
	}

	protected List<CategorizedDescriptor> queryFor(ModelType type,
			String idField, String table, Set<Long> toFind, String... inFields) {
		return Search.on(db).queryFor(type, idField, table, toFind,
				inFields);
	}

	protected Set<Long> queryForIds(String idField, String table,
			Set<Long> toFind, String... inFields) {
		return Search.on(db)
				.queryForIds(idField, table, toFind, inFields);
	}

	protected List<CategorizedDescriptor> loadDescriptors(ModelType type,
			Set<Long> ids) {
		return Search.on(db).loadDescriptors(type, ids);
	}

	protected Set<Long> getIds(String table) {
		return Search.on(db).getIds(table);
	}

	private Set<Long> toIdSet(List<? extends CategorizedDescriptor> descriptors) {
		Set<Long> ids = new HashSet<>();
		for (CategorizedDescriptor descriptor : descriptors)
			ids.add(descriptor.id);
		return ids;
	}

}
