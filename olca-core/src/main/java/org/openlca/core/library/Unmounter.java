package org.openlca.core.library;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.util.CategoryContentTest;

public class Unmounter {

	private final IDatabase database;
	private final CategoryContentTest test;
	private final CategoryDao categoryDao;
	private final EnumMap<ModelType, RootEntityDao<?, ?>> daos = new EnumMap<>(ModelType.class);

	public Unmounter(IDatabase database) {
		this.database = database;
		this.test = new CategoryContentTest(database);
		this.categoryDao = new CategoryDao(database);
		for (var type : ModelType.values()) {
			this.daos.put(type, Daos.root(database, type));
		}
	}

	public void unmountUnsafe(Library lib) {
		var rootCategories = categoryDao.getRootCategories();
		var libraryCategories = collectCategories(rootCategories, lib);
		for (var type : ModelType.values()) {
			if (type == ModelType.CATEGORY)
				continue;
			var dao = daos.get(type);
			for (var descriptor : dao.getDescriptors()) {
				if (!descriptor.isFromLibrary() || !lib.name().equals(descriptor.library))
					continue;
				dao.delete(descriptor.id);
			}
		}
		for (var category : libraryCategories) {
			categoryDao.delete(category);
		}
		database.removeLibrary(lib.name());
	}

	private List<Category> collectCategories(List<Category> in, Library lib) {
		var categories = new ArrayList<Category>();
		for (var category : in) {
			categories.addAll(collectCategories(category, lib));
		}
		return categories;
	}

	private List<Category> collectCategories(Category category, Library lib) {
		var categories = new ArrayList<Category>();
		if (hasOnlyLibraryContent(category, lib)) {
			categories.add(category);
		}
		for (var child : category.childCategories) {
			categories.addAll(collectCategories(child, lib));
		}
		return categories;
	}

	private boolean hasOnlyLibraryContent(Category category, Library lib) {
		if (!test.hasOnlyLibraryContent(category, lib.name()))
			return false;
		for (var child : category.childCategories)
			if (!hasOnlyLibraryContent(child, lib))
				return false;
		return true;
	}

}