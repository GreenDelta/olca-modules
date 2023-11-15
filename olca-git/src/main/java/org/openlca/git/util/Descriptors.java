package org.openlca.git.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Categories;
import org.openlca.util.Categories.PathBuilder;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Descriptors {

	private static final Logger log = LoggerFactory.getLogger(Descriptors.class);
	private static final Descriptor NULL;
	private final IDatabase database;
	private final EnumMap<ModelType, DescriptorsMaps> cache = new EnumMap<>(ModelType.class);
	private final EnumMap<ModelType, List<Category>> rootCategories = new EnumMap<>(ModelType.class);
	private final Map<String, Category> categoriesByPath = new HashMap<>();
	public final PathBuilder categoryPaths;

	static {
		NULL = new Descriptor();
		NULL.lastChange = -1;
		NULL.version = -1;
	}

	private Descriptors(IDatabase database) {
		this.database = database;
		this.categoryPaths = Categories.pathsOf(database);
		loadCategories();
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
			return cache.computeIfAbsent(type, this::load).byRefId
					.getOrDefault(refId, NULL);
		}
	}

	public Set<Descriptor> get(ModelType type) {
		if (type == null)
			return new HashSet<>();
		synchronized (cache) {
			return cache.computeIfAbsent(type, this::load).byCategory
					.getOrDefault(null, new HashSet<>());
		}
	}

	public Set<Descriptor> get(Category category) {
		if (category == null)
			return new HashSet<>();
		synchronized (cache) {
			return cache.computeIfAbsent(category.modelType, this::load).byCategory
					.getOrDefault(category.id, new HashSet<>());
		}
	}

	public List<Category> getCategories(ModelType type) {
		return rootCategories.getOrDefault(type, new ArrayList<>());
	}

	public Category getCategory(String path) {
		return categoriesByPath.get(path);
	}

	private DescriptorsMaps load(ModelType type) {
		var descriptors = new DescriptorsMaps();
		for (var descriptor : database.getDescriptors(type.getModelClass())) {
			var refId = descriptor.refId;
			if (descriptors.byRefId.containsKey(refId)) {
				var existing = descriptors.byRefId.get(refId).id;
				log.warn("Duplicate descriptor for " + type + ": [" + existing + ", " + refId + "]");
			}
			descriptors.byRefId.put(descriptor.refId, descriptor);
			descriptors.byCategory
					.computeIfAbsent(descriptor.category, k -> new HashSet<>())
					.add(descriptor);
		}
		return descriptors;
	}

	private void loadCategories() {
		for (var category : new CategoryDao(database).getAll()) {
			if (category.category == null) {
				rootCategories.computeIfAbsent(category.modelType, k -> new ArrayList<>())
						.add(category);
			}
			categoriesByPath.put(toPath(category), category);
		}
	}

	private String toPath(Category category) {
		var paths = Categories.path(category);
		paths.add(0, category.modelType.name());
		return Strings.join(paths, '/');
	}

	private class DescriptorsMaps {

		private final Map<String, Descriptor> byRefId = new HashMap<>();
		private final Map<Long, Set<Descriptor>> byCategory = new HashMap<>();

	}

}
