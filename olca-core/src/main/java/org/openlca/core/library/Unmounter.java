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
import org.openlca.core.database.ModelReferences;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.TypedRefId;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.util.CategoryContentTest;
import org.openlca.util.TypedRefIdMap;

public class Unmounter {

	private final IDatabase database;
	private final Retention retention;
	private final String lib;
	private final LibReader reader;
	private final ProcessDao processDao;
	private final ImpactMethodDao methodDao;
	private CategoryContentTest categoryTest;
	private Map<Long, Category> categoriesToDelete;
	private TypedRefIdMap<Boolean> keep;
	private ModelReferences references;

	public static void keepNone(IDatabase database, String lib) {
		if (lib == null)
			return;
		new Unmounter(database, Retention.KEEP_NONE, lib, null).unmount();
	}

	public static void keepUsed(IDatabase database, LibReader reader) {
		if (reader == null)
			return;
		new Unmounter(database, Retention.KEEP_USED, reader.libraryName(), reader).unmount();
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
		this.processDao = new ProcessDao(database);
		this.methodDao = new ImpactMethodDao(database);
	}

	private void init() {
		this.categoriesToDelete = collectLibraryCategories();
		this.keep = new TypedRefIdMap<>();
		this.references = retention == Retention.KEEP_USED
				? ModelReferences.scan(database)
				: null;
		determineToKeep();
	}

	private void unmount() {
		init();
		for (var type : ModelType.values()) {
			if (type == ModelType.CATEGORY)
				continue;
			untag(type);
		}
		new CategoryDao(database).deleteAll(categoriesToDelete.values());
		database.removeDataPackage(lib);
	}

	private void untag(ModelType type) {
		var dao = Daos.root(database, type);
		var untag = new HashSet<String>();
		for (var descriptor : dao.getDescriptors()) {
			if (!lib.equals(descriptor.dataPackage))
				continue;
			if (!keep(descriptor)) {
				dao.delete(descriptor.id);
			} else {
				untag.add(descriptor.refId);
				restoreFromLibrary(descriptor);
			}
		}
		if (!untag.isEmpty()) {
			Retagger.updateAllOf(database, type, untag, null);
		}
	}

	private boolean keep(RootDescriptor descriptor) {
		if (retention == Retention.KEEP_NONE)
			return false;
		if (retention == Retention.KEEP_ALL)
			return true;
		var keepRef = keep.get(descriptor.type, descriptor.refId);
		return keepRef != null && keepRef;
	}

	private void restoreFromLibrary(RootDescriptor descriptor) {
		keepCategory(descriptor.category);
		if (descriptor.type == ModelType.PROCESS) {
			var process = processDao.getForId(descriptor.id);
			Libraries.fillExchangesOf(database, reader, process);
			processDao.update(process);
		} else if (descriptor.type == ModelType.IMPACT_METHOD) {
			var method = methodDao.getForId(descriptor.id);
			for (var impact : method.impactCategories) {
				Libraries.fillFactorsOf(database, reader, impact);
			}
			methodDao.update(method);
		}
	}

	private void determineToKeep() {
		if (retention != Retention.KEEP_USED)
			return;
		for (var type : ModelType.values()) {
			for (var descriptor : Daos.root(database, type).getDescriptors()) {
				var ref = new TypedRefId(descriptor.type, descriptor.refId);
				if (!lib.equals(descriptor.dataPackage))
					continue;
				if (keep.contains(ref))
					continue;
				references.iterateUsages(ref, usage -> {
					if (!lib.equals(usage.dataPackage)) {
						keep(usage);
						return false;
					}
					return true;
				});
				if (!keep.contains(ref)) {
					keep.put(ref, false);
				}
			}
		}
	}

	private void keep(TypedRefId ref) {
		if (keep.contains(ref))
			return;
		keep.put(ref, true);
		references.iterateReferences(ref, reference -> {
			if (!lib.equals(reference.dataPackage))
				return;
			keep(reference);
		});
	}

	private Map<Long, Category> collectLibraryCategories() {
		if (retention == Retention.KEEP_ALL)
			return new HashMap<>();
		this.categoryTest = new CategoryContentTest(database);
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
		if (!categoryTest.hasOnlyDataPackageContent(category, lib))
			return false;
		for (var child : category.childCategories)
			if (!hasOnlyLibraryContent(child))
				return false;
		return true;
	}

	private void keepCategory(Long id) {
		if (id == null)
			return;
		var category = categoriesToDelete.remove(id);
		if (category != null && category.category != null) {
			keepCategory(category.category.id);
		}
	}

	public enum Retention {

		KEEP_NONE, KEEP_USED, KEEP_ALL

	}

}
