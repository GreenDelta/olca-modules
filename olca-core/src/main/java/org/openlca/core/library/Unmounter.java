package org.openlca.core.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.util.CategoryContentTest;

public class Unmounter {

	private final IDatabase database;
	private final Retention retention;
	private final String lib;
	private final LibReader reader;
	private final CategoryContentTest test;

	public static void keepNone(IDatabase database, String lib) {
		if (lib == null)
			return;
		new Unmounter(database, Retention.KEEP_NONE, lib, null).unmount();
	}

	public static void keepAll(IDatabase database, LibReader reader) {
		if (reader == null)
			return;
		new Unmounter(database, Retention.KEEP_ALL, reader.libraryName(), reader).unmount();
	}

	private Unmounter(IDatabase database, Retention retention, String lib, LibReader reader) {
		this.database = database;
		this.retention = retention;
		this.lib = lib;
		this.reader = reader;
		this.test = new CategoryContentTest(database);
	}

	protected void unmount() {
		var categoriesToDelete = collectLibraryCategories();
		var processDao = new ProcessDao(database);
		var methodDao = new ImpactMethodDao(database);
		for (var type : ModelType.values()) {
			var untag = new HashSet<String>();
			if (type == ModelType.CATEGORY)
				continue;
			var dao = Daos.root(database, type);
			for (var descriptor : dao.getDescriptors()) {
				if (!descriptor.isFromLibrary() || !lib.equals(descriptor.library))
					continue;
				if (!keep(descriptor)) {
					dao.delete(descriptor.id);
				} else {
					untag.add(descriptor.refId);
					removeFromMap(categoriesToDelete, categoriesToDelete.get(descriptor.category));
					if (type == ModelType.PROCESS) {
						var process = processDao.getForId(descriptor.id);
						Libraries.fillExchangesOf(database, reader, process);
						processDao.update(process);
					} else if (type == ModelType.IMPACT_METHOD) {
						var method = methodDao.getForId(descriptor.id);
						for (var impact : method.impactCategories) {
							Libraries.fillFactorsOf(database, reader, impact);
						}
						methodDao.update(method);
					}
				}
			}
			if (!untag.isEmpty()) {
				Retagger.updateAllOf(database, type, untag, null);
			}
		}
		new CategoryDao(database).deleteAll(categoriesToDelete.values());
		database.removeLibrary(lib);
	}

	private boolean keep(RootDescriptor descriptor) {
		if (retention == Retention.KEEP_NONE)
			return false;
		return true;
	}

	private Map<Long, Category> collectLibraryCategories() {
		if (retention == Retention.KEEP_ALL)
			return new HashMap<>();
		var categories = new ArrayList<Category>();
		for (var category : new CategoryDao(database).getRootCategories()) {
			categories.addAll(collectCategories(category));
		}
		return categories.stream()
				.collect(Collectors.toMap(c -> c.id, c -> c));
	}

	private List<Category> collectCategories(Category category) {
		var categories = new ArrayList<Category>();
		if (hasOnlyLibraryContent(category)) {
			categories.add(category);
		}
		for (var child : category.childCategories) {
			categories.addAll(collectCategories(child));
		}
		return categories;
	}

	private boolean hasOnlyLibraryContent(Category category) {
		if (!test.hasOnlyLibraryContent(category, lib))
			return false;
		for (var child : category.childCategories)
			if (!hasOnlyLibraryContent(child))
				return false;
		return true;
	}

	private void removeFromMap(Map<Long, Category> map, Category category) {
		if (category == null)
			return;
		map.remove(category.id);
		removeFromMap(map, category.category);
	}

	public enum Retention {

		KEEP_NONE, /* KEEP_USED, */ KEEP_ALL;

	}

}