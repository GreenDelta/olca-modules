package org.openlca.ipc;

import java.util.HashMap;

public class Cache {

	private final HashMap<String, Object> cache = new HashMap<>();

	public Object get(String id) {
		return cache.get(id);
	}

	public void put(String id, Object obj) {
		cache.put(id, obj);
	}

	public Object remove(String id) {
		return cache.remove(id);
	}

}
