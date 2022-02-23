package org.openlca.core.io;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.descriptors.Descriptor;

public class CachedDbEntityResolver implements EntityResolver {

	private final DbEntityResolver resolver;
	private final Set<Class<? extends CategorizedEntity>> cachedTypes;
	private final Map<Class<? extends CategorizedEntity>, Map<String, Object>> cache;

	private CachedDbEntityResolver(
		IDatabase db, Class<? extends CategorizedEntity>[] cachedTypes) {
		this.resolver = DbEntityResolver.of(db);
		this.cachedTypes = Set.of(cachedTypes);
		this.cache = new HashMap<>();
	}

	@Override
	public <T extends RefEntity> T get(Class<T> type, String refId) {
		return null;
	}

	public void update(CategorizedEntity e) {
		if (e == null)
			return;
		var m = cache.get(e.getClass());
		if (m == null)
			return;
		m.remove(e.refId);

	}

	@Override
	public <T extends RefEntity> Descriptor getDescriptor(
		Class<T> type, String refId) {

		return EntityResolver.super.getDescriptor(type, refId);
	}
}
