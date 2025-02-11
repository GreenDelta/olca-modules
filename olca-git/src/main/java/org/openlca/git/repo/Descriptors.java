package org.openlca.git.repo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.TypedRefId;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.util.Path;
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

	public RootDescriptor get(TypedRefId ref) {
		if (ref.type == null || ref.refId == null || ref.refId.strip().isEmpty())
			return null;
		synchronized (cache) {
			return cache.computeIfAbsent(ref.type, this::load).byRefId
					.get(ref.refId);
		}
	}

	public boolean isFromLibrary(TypedRefId ref) {
		var descriptor = get(ref);
		return descriptor != null && descriptor.isFromLibrary();
	}
	
	public boolean isOnlyInLibraries(Category category) {
		return isOnlyInLibraries(category, new HashSet<>());
	}

	public boolean isOnlyInLibraries(Category category, Set<String> libraries) {
		if (category == null)
			return false;
		var isOnlyInLibs = false;
		for (var model : get(category)) {
			if (!model.isFromLibrary())
				return false;
			if (!libraries.contains(model.library))
				return false;
			isOnlyInLibs = true;
		}
		for (var child : category.childCategories) {
			if (!isOnlyInLibraries(child, libraries))
				return false;
			isOnlyInLibs = true;
		}
		return isOnlyInLibs;		
	}
	
	public String getLibrary(TypedRefId ref) {
		var descriptor = get(ref);
		if (descriptor == null)
			return null;
		return descriptor.library;
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
		if (category == null || category.modelType == null)
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
			if (category.modelType == null)
				continue;
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
