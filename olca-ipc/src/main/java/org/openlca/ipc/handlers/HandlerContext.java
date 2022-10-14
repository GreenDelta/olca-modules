package org.openlca.ipc.handlers;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.services.CalculationQueue;
import org.openlca.core.services.JsonResultService;
import org.openlca.core.services.ServerConfig;
import org.openlca.ipc.Cache;
import org.openlca.ipc.Server;

import java.util.UUID;

public record HandlerContext(
	Server server,
	ServerConfig config,
	JsonResultService results,
	Cache cache) {

	public Object getCached(String id) {
		return cache.get(id);
	}

	public IDatabase db() {
		return config.db();
	}

	public LibraryDir libDir() {
		return config.dataDir().getLibraryDir();
	}

	public <T> T getCached(Class<T> clazz, String id) {
		var obj = cache.get(id);
		return clazz.isInstance(obj)
			? clazz.cast(obj)
			: null;
	}

	public String cache(Object object) {
		var id = UUID.randomUUID().toString();
		cache.put(id, object);
		return id;
	}

	public Object popCached(String id) {
		return cache.remove(id);
	}
}
