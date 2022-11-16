package org.openlca.io.refdata;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.csv.CSVParser;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.CategorySync;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;

class ImportConfig {

	private final ImportLog log = new ImportLog();

	private final File folder;
	private final IDatabase db;
	private final CategorySync categories;
	private final EnumMap<ModelType, Map<String, RootEntity>> entities;

	private ImportConfig(File folder, IDatabase db) {
		this.folder = folder;
		this.db	 = db;
		this.categories = CategorySync.of(db);
		this.entities = new EnumMap<>(ModelType.class);
	}

	static ImportConfig of(File folder, IDatabase db) {
		return new ImportConfig(folder, db);
	}

	ImportLog log() {
		return log;
	}

	IDatabase db() {
		return db;
	}

	Category category(ModelType type, String path) {
		return categories.get(type, path);
	}

	void eachRow(String file, Consumer<CsvRow> fn) {
		var f = new File(folder, file);
		if (!f.exists()) {
			log.info("file " + f + " does not exist; skipped");
			return;
		}
		try (var reader = new FileReader(f, StandardCharsets.UTF_8);
				 var parser = new CSVParser(reader, Csv.format())) {
			for (var row : parser) {
				fn.accept(new CsvRow(row));
			}
		} catch (Exception e) {
			log.error("failed to parse file " + f, e);
		}
	}

	void insert(Iterable<? extends RootEntity> entities) {
		db.transaction(em -> {
			for (var entity : entities) {
				em.persist(entity);
			}
		});
	}

}
