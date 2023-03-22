package org.openlca.git.util;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;

public class Descriptors {

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
		return cache.computeIfAbsent(type, this::load)
				.getOrDefault(refId, NULL);
	}

	private Map<String, Descriptor> load(ModelType type) {
		return database.getDescriptors(type.getModelClass()).stream()
				.collect(Collectors.toMap(d -> d.refId, Function.identity()));
	}

}
