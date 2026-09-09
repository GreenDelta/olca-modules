package org.openlca.core.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.util.CategoryContentTest;

public class Unmounter {

	private final IDatabase db;
	private final Retention retention;
	private final String lib;
	private final LibReader reader;
	private final UnmounterKeepSet keepSet;
	private final ProcessDao processDao;
	private final ImpactMethodDao methodDao;

	private final CategoryContentTest categoryTest;
	private final Map<Long, Category> categoriesToDelete;

	public static void keepNone(IDatabase db, String lib) {
		if (lib == null)
			return;
		new Unmounter(db, Retention.KEEP_NONE, lib, null).unmount();
	}

	public static void keepUsed(IDatabase db, LibReader reader) {
		if (reader == null)
			return;
		new Unmounter(db, Retention.KEEP_USED, reader.libraryName(), reader).unmount();
	}

	public static void keepAll(IDatabase db, LibReader reader) {
		if (reader == null)
			return;
		new Unmounter(db, Retention.KEEP_ALL, reader.libraryName(), reader).unmount();
	}

	private Unmounter(IDatabase db, Retention retention, String lib, LibReader reader) {
		this.db = db;
		this.retention = retention;
		this.lib = lib;
		this.reader = reader;
		this.processDao = new ProcessDao(db);
		this.methodDao = new ImpactMethodDao(db);
		this.keepSet = UnmounterKeepSet.of(retention, db, reader);
		this.categoryTest = new CategoryContentTest(db);
		this.categoriesToDelete = collectLibraryCategories();
	}

	private void unmount() {

		// iterate through the types in "deletion order" so that projects, epd, etc.
		// are deleted first and things like units, sources etc. last
		var types = Arrays.stream(ModelType.values())
			.sorted((a, b) -> Integer.compare(delOrd(a), delOrd(b)))
			.toList();

		// first, restore entities; important: we cannot restore and delete in one
		// run as restoring needs library access
		var removals = new ArrayList<RootDescriptor>();
		var restored = new EnumMap<ModelType, Set<String>>(ModelType.class);
		for (var type : types) {
			if (type == ModelType.CATEGORY)
				continue;
			var rs = restore(type, removals);
			if (!rs.isEmpty()) {
				restored.put(type, rs);
			}
		}

		// delete unused library data
		deleteAll(removals);

		// delete empty categories
		new CategoryDao(db).deleteAll(categoriesToDelete.values());

		// untag restored entities
		for (var e : restored.entrySet()) {
			var type = e.getKey();
			var ids = e.getValue();
			Retagger.updateAllOf(db, type, ids, null);
		}

		// finally, remove the library
		db.removeLibrary(lib);
	}

	private void deleteAll(List<RootDescriptor> removals) {
		ModelType type = null;
		RootEntityDao<?, ?> dao = null;
		for (var rem : removals) {
			if (rem.type == null)
				continue;
			dao = type != rem.type
				? Daos.root(db, rem.type.getModelClass())
				: dao;
			type = rem.type;
			dao.delete(rem.id);
		}
	}

	private Set<String> restore(ModelType type, List<RootDescriptor> removals) {
		var dao = Daos.root(db, type);
		var restored = new HashSet<String>();
		for (var d : dao.getDescriptors()) {
			if (!lib.equals(d.library))
				continue;
			if (keepSet.has(d)) {
				restoreFromLibrary(d);
				restored.add(d.refId);
			} else {
				removals.add(d);
			}
		}
		return restored;
	}

	private void restoreFromLibrary(RootDescriptor descriptor) {
		keepCategory(descriptor.category);
		if (descriptor.type == ModelType.PROCESS) {
			var process = processDao.getForId(descriptor.id);
			Libraries.fillExchangesOf(db, reader, process);
			processDao.update(process);
		} else if (descriptor.type == ModelType.IMPACT_METHOD) {
			var method = methodDao.getForId(descriptor.id);
			for (var impact : method.impactCategories) {
				Libraries.fillFactorsOf(db, reader, impact);
			}
			methodDao.update(method);
		}
	}

	private Map<Long, Category> collectLibraryCategories() {
		if (retention == Retention.KEEP_ALL)
			return new HashMap<>();
		var categories = new ArrayList<Category>();
		for (var category : new CategoryDao(db).getRootCategories()) {
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
		if (!categoryTest.hasOnlyLibraryContent(category, lib))
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

	private int delOrd(ModelType type) {
		return switch (type) {
			case ModelType.PROJECT -> 1;
			case ModelType.EPD -> 2;
			case ModelType.RESULT -> 3;
			case ModelType.IMPACT_METHOD -> 4;
			case ModelType.IMPACT_CATEGORY -> 5;
			case ModelType.PRODUCT_SYSTEM -> 6;
			case ModelType.PROCESS -> 7;
			case ModelType.SOCIAL_INDICATOR -> 8;
			case ModelType.FLOW -> 9;
			case ModelType.FLOW_PROPERTY -> 10;
			case ModelType.UNIT_GROUP -> 11;
			case ModelType.CURRENCY -> 12;

			case ModelType.ACTOR,
					 ModelType.SOURCE,
					 ModelType.LOCATION,
					 ModelType.PARAMETER,
					 ModelType.DQ_SYSTEM -> 99;
			case CATEGORY -> -99; // is handled otherwise
		};
	}

	public enum Retention {

		KEEP_NONE, KEEP_USED, KEEP_ALL

	}

}
