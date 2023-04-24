package org.openlca.git.util;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Descriptors {

	private static final Logger log = LoggerFactory.getLogger(Descriptors.class);
	private static final Descriptor NULL;
	private final IDatabase database;
	private final EnumMap<ModelType, Map<String, Descriptor>> cache = new EnumMap<>(ModelType.class);

	static {
		NULL = new Descriptor();
		NULL.lastChange = -1;
		NULL.version = -1;
	}

	private Descriptors(IDatabase database) {
		this.database = database;
	}

	public static Descriptors of(IDatabase database) {
		return new Descriptors(database);
	}

	public Descriptor get(String path) {
		return get(new TypedRefId(path));
	}

	public Descriptor get(TypedRefId typedRefId) {
		return get(typedRefId.type, typedRefId.refId);
	}

	public Descriptor get(ModelType type, String refId) {
		if (type == null || refId == null || refId.strip().isEmpty())
			return NULL;
		synchronized (cache) {
			return cache.computeIfAbsent(type, this::load)
					.getOrDefault(refId, NULL);
		}
	}

	private Map<String, Descriptor> load(ModelType type) {
		var map = new HashMap<String, Descriptor>();
		for (var descriptor : database.getDescriptors(type.getModelClass())) {
			var refId = descriptor.refId;
			if (map.containsKey(refId)) {
				var existing = map.get(refId).id;
				log.warn("Duplicate descriptor for " + type + ": [" + existing + ", " + refId + "]");
			}
			map.put(descriptor.refId, descriptor);
		}
		return map;
	}

}
