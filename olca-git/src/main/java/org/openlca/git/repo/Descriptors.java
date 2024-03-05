package org.openlca.git.repo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.util.Path;
import org.openlca.git.util.TypedRefId;
import org.openlca.util.Categories;
import org.openlca.util.Categories.PathBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Descriptors {

	private static final Logger log = LoggerFactory.getLogger(Descriptors.class);
	private final IDatabase database;
	private final EnumMap<ModelType, DescriptorsMaps> cache = new EnumMap<>(ModelType.class);
	private final EnumMap<ModelType, List<Category>> rootCategories = new EnumMap<>(ModelType.class);
	private final Map<String, Category> categoriesByPath = new HashMap<>();
	private final Map<Long, Category> categoriesById = new HashMap<>();
	public PathBuilder categoryPaths;

	private Descriptors(IDatabase database) {
		this.database = database;
		reload();
	}

	public void reload() {
		cache.clear();
		rootCategories.clear();
		categoriesByPath.clear();
		categoriesById.clear();
		categoryPaths = Categories.pathsOf(database);
		loadCategories();
	}

	public void reload(ModelType type) {
		cache.remove(type);
		rootCategories.clear();
		categoriesByPath.clear();
		categoriesById.clear();
		loadCategories();
	}

	static Descriptors of(IDatabase database) {
		return new Descriptors(database);
	}

	public RootDescriptor get(String path) {
		return get(new TypedRefId(path));
	}

	public RootDescriptor get(TypedRefId typedRefId) {
		return get(typedRefId.type, typedRefId.refId);
	}

	public RootDescriptor get(ModelType type, String refId) {
		if (type == null || refId == null || refId.strip().isEmpty())
			return null;
		synchronized (cache) {
			return cache.computeIfAbsent(type, this::load).byRefId
					.get(refId);
		}
	}

	public Set<RootDescriptor> get(ModelType type) {
		if (type == null)
			return new HashSet<>();
		synchronized (cache) {
			return cache.computeIfAbsent(type, this::load).byCategory
					.getOrDefault(null, new HashSet<>());
		}
	}

	public Set<RootDescriptor> get(Category category) {
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

	public Category getCategory(Long id) {
		return categoriesById.get(id);
	}

	public void forEach(Consumer<RootDescriptor> consumer) {
		for (var modelType : ModelType.values()) {
			synchronized (cache) {
				cache.computeIfAbsent(modelType, this::load).byRefId.values().forEach(consumer);
			}
		}
	}

	private DescriptorsMaps load(ModelType type) {
		var descriptors = new DescriptorsMaps();
		if (database == null)
			return descriptors;
		var fromDb = type == ModelType.PARAMETER
				? new ParameterDao(database).getGlobalDescriptors()
				: database.getDescriptors(type.getModelClass());
		for (var descriptor : fromDb) {
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
		if (database == null)
			return;
		for (var category : new CategoryDao(database).getAll()) {
			if (category.category == null) {
				rootCategories.computeIfAbsent(category.modelType, k -> new ArrayList<>())
						.add(category);
			}
			categoriesByPath.put(Path.of(category), category);
			categoriesById.put(category.id, category);
		}
	}

	private class DescriptorsMaps {

		private final Map<String, RootDescriptor> byRefId = new HashMap<>();
		private final Map<Long, Set<RootDescriptor>> byCategory = new HashMap<>();

	}

}
