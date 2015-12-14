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

	private IDatabase database;

	BaseUseSearch(IDatabase database) {
		this.database = database;
	}

	@Override
	public List<CategorizedDescriptor> findUses(T descriptor) {
		if (descriptor == null || descriptor.getId() == 0l)
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
		if (id == 0l)
			return Collections.emptyList();
		return findUses(Collections.singleton(id));
	}

	protected List<CategorizedDescriptor> queryFor(ModelType type,
			Set<Long> toFind, String... inFields) {
		return Search.on(database).queryFor(type, toFind, inFields);
	}

	protected List<CategorizedDescriptor> queryFor(ModelType type,
			String idField, String table, Set<Long> toFind, String... inFields) {
		return Search.on(database).queryFor(type, idField, table, toFind,
				inFields);
	}

	protected Set<Long> queryForIds(String idField, String table,
			Set<Long> toFind, String... inFields) {
		return Search.on(database)
				.queryForIds(idField, table, toFind, inFields);
	}

	protected List<CategorizedDescriptor> loadDescriptors(ModelType type,
			Set<Long> ids) {
		return Search.on(database).loadDescriptors(type, ids);
	}

	private Set<Long> toIdSet(List<? extends CategorizedDescriptor> descriptors) {
		Set<Long> ids = new HashSet<>();
		for (CategorizedDescriptor descriptor : descriptors)
			ids.add(descriptor.getId());
		return ids;
	}

}
