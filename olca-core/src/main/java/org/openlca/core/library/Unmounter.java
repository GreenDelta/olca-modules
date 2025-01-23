package org.openlca.core.library;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.util.CategoryContentTest;

public class Unmounter {

	protected final IDatabase database;
	protected final Retention retention;
	private final CategoryContentTest test;

	public static SimpleUnmounter keepNone(IDatabase database) {
		return new SimpleUnmounter(database);
	}

	// TODO
	// public static RetainingUnmounter keepUsed(IDatabase database) {
	// }

	public static RetainingUnmounter keepAll(IDatabase database) {
		return new RetainingUnmounter(database, Retention.KEEP_ALL);
	}

	private Unmounter(IDatabase database, Retention retention) {
		this.database = database;
		this.retention = retention;
		this.test = new CategoryContentTest(database);
	}

	public static class SimpleUnmounter extends Unmounter {

		private SimpleUnmounter(IDatabase database) {
			super(database, Retention.KEEP_NONE);
		}

		public void unmount(String lib) {
			if (retention != Retention.KEEP_NONE)
				throw new UnsupportedOperationException(
						"Unmounting library by name is only possible with Retention.KEEP_NONE");
			if (lib == null)
				return;
			unmount(lib, null);
		}

	}

	public static class RetainingUnmounter extends Unmounter {

		private RetainingUnmounter(IDatabase database, Retention retention) {
			super(database, retention);
		}

		public void unmount(LibReader lib) {
			if (lib == null)
				return;
			unmount(lib.libraryName(), lib);
		}

	}

	protected void unmount(String lib, LibReader reader) {
		var categoriesToDelete = collectLibraryCategories(lib).stream()
				.collect(Collectors.toMap(c -> c.id, c -> c));
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
				if (retention == Retention.KEEP_NONE) {
					dao.delete(descriptor.id);
				} else {
					untag.add(descriptor.refId);
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

	private List<Category> collectLibraryCategories(String lib) {
		if (retention == Retention.KEEP_ALL)
			return new ArrayList<>();
		var categories = new ArrayList<Category>();
		for (var category : new CategoryDao(database).getRootCategories()) {
			categories.addAll(collectCategories(category, lib));
		}
		return categories;
	}

	private List<Category> collectCategories(Category category, String lib) {
		var categories = new ArrayList<Category>();
		if (hasOnlyLibraryContent(category, lib)) {
			categories.add(category);
		}
		for (var child : category.childCategories) {
			categories.addAll(collectCategories(child, lib));
		}
		return categories;
	}

	private boolean hasOnlyLibraryContent(Category category, String lib) {
		if (!test.hasOnlyLibraryContent(category, lib))
			return false;
		for (var child : category.childCategories)
			if (!hasOnlyLibraryContent(child, lib))
				return false;
		return true;
	}

	public enum Retention {

		KEEP_NONE, /* KEEP_USED, */ KEEP_ALL;

	}

}