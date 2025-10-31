package org.openlca.io.refdata;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.csv.CSVParser;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.CategorySync;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.Strings;

class ImportConfig {

	private final ImportLog log = new ImportLog();

	private final File dir;
	private final IDatabase db;
	private final CategorySync categories;
	private final Map<Class<?>, Map<String, RootEntity>> cache;

	private ImportConfig(File folder, IDatabase db) {
		this.dir = folder;
		this.db	 = db;
		this.categories = CategorySync.of(db);
		this.cache = new HashMap<>();
	}

	static ImportConfig of(File folder, IDatabase db) {
		return new ImportConfig(folder, db);
	}

	IDatabase db() {
		return db;
	}

	File dir() {
		return dir;
	}

	ImportLog log() {
		return log;
	}

	Category category(ModelType type, String path) {
		return categories.get(type, path);
	}

	void eachRowOf(String file, Consumer<CsvRow> fn) {
		eachRowOf(new File(dir, file), fn);
	}

	void eachRowOf(File file, Consumer<CsvRow> fn) {
		if (!file.exists()) {
			log.info("file " + file + " does not exist; skipped");
			return;
		}
		log.info("read file: " + file);
		try (var reader = new FileReader(file, StandardCharsets.UTF_8);
				 var parser = new CSVParser(reader, Csv.format())) {
			boolean first = true;
			for (var row : parser) {
				if (first) {
					// skip header
					first = false;
					continue;
				}
				fn.accept(new CsvRow(row));
			}
		} catch (Exception e) {
			log.error("failed to parse file " + file, e);
		}
	}

	void insert(RootEntity e) {
		insert(List.of(e));
	}

	void insert(List<? extends RootEntity> entities) {
		if (entities.isEmpty())
			return;
		var type = entities.get(0).getClass();
		var map = cache.computeIfAbsent(type, t -> new HashMap<>());
		boolean refByName = supportRefByName(type);

		db.transaction(em -> {
			for (var entity : entities) {
				em.persist(entity);
				map.put(entity.refId, entity);
				if (refByName && Strings.isNotBlank(entity.name)) {
					map.put(entity.name, entity);
				}
			}
		});
	}

	void update(RootEntity e) {
		if (e == null)
			return;
		db.update(e);
		cache(e);
	}

	void reload(RootEntity e) {
		if (e == null)
			return;
		var reloaded = db.get(e.getClass(), e.id);
		cache(reloaded);
	}

	private void cache(RootEntity e) {
		if (e == null)
			return;
		var type = e.getClass();
		var map = cache.computeIfAbsent(type, t -> new HashMap<>());
		if (supportRefByName(type) && Strings.isNotBlank(e.name)) {
			map.put(e.name, e);
		}
	}

	<T extends RootEntity> T get(Class<T> type, String id) {
		var map = cache.get(type);
		if (map == null)
			return null;
		var obj = map.get(id);
		return obj != null
				? type.cast(obj)
				: null;
	}

	private boolean supportRefByName(Class<? extends  RootEntity> type) {
		return type.equals(UnitGroup.class)
				|| type.equals(FlowProperty.class)
				|| type.equals(Currency.class)
				|| type.equals(Location.class)
				|| type.equals(ImpactMethod.class);
	}

}
