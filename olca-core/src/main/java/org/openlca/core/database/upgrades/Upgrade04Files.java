package org.openlca.core.database.upgrades;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Upgrade04Files {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final File root;
	private final FileStore store;
	private final IDatabase db;

	private Upgrade04Files(File root, IDatabase db) {
		this.root = root;
		store = new FileStore(root);
		this.db = db;
	}

	public static void apply(IDatabase db) {
		if (db == null)
			return;
		File root = db.getFileStorageLocation();
		if (root == null || !root.exists() || !root.isDirectory())
			return;
		new Upgrade04Files(root, db).exec();
	}

	private void exec() {
		log.info("update file store {} of database {}", root, db);
		try {
			copyShapeFiles();
			copyLayoutFiles();
			copySourceDocs();
		} catch (Exception e) {
			throw new RuntimeException("failed to copy resource files", e);
		}
	}

	private void copyShapeFiles() throws IOException {
		File shapeFileDir = new File(root, "shapefiles");
		if (!shapeFileDir.exists())
			return;
		var sourceDirs = shapeFileDir.listFiles();
		if (sourceDirs == null)
			return;
		for (File sourceDir : sourceDirs) {
			if (!sourceDir.isDirectory())
				continue;
			String id = sourceDir.getName();
			File targetDir = store.getFolder(ModelType.IMPACT_METHOD, id);
			if (!targetDir.exists()) {
				Files.createDirectories(targetDir.toPath());
			}
			var sourceFiles = sourceDir.listFiles();
			if (sourceFiles == null)
				continue;
			for (File source : sourceFiles) {
				File target = new File(targetDir, source.getName());
				Files.copy(source.toPath(), target.toPath());
			}
		}
	}

	private void copyLayoutFiles() throws IOException {
		File layoutDir = new File(root, "layouts");
		if (!layoutDir.exists())
			return;
		var layoutFiles = layoutDir.listFiles();
		if (layoutFiles == null)
			return;
		for (File file : layoutFiles) {
			String name = file.getName(); // name = <id>.json
			String id = name.substring(0, name.length() - 5);
			File dir = store.getFolder(ModelType.PRODUCT_SYSTEM, id);
			if (!dir.exists()) {
				Files.createDirectories(dir.toPath());
			}
			Files.copy(file.toPath(), new File(dir, "layout.json").toPath());
		}
	}

	private void copySourceDocs() throws IOException {
		File docDir = new File(root, "external_docs");
		if (!docDir.exists())
			return;
		var docFiles = docDir.listFiles();
		if (docFiles == null)
			return;
		Map<String, List<String>> map = getSourceDocs();
		for (File file : docFiles) {
			String name = file.getName();
			List<String> sourceIds = map.get(name);
			if (sourceIds == null)
				continue;
			for (String id : sourceIds) {
				File dir = store.getFolder(ModelType.SOURCE, id);
				if (!dir.exists()) {
					Files.createDirectories(dir.toPath());
				}
				Files.copy(file.toPath(), new File(dir, name).toPath());
			}
		}
	}

	private Map<String, List<String>> getSourceDocs() {
		Map<String, List<String>> map = new HashMap<>();
		String query = "select ref_id, external_file from tbl_sources";
		NativeSql.on(db).query(query, r -> {
			String id = r.getString(1);
			String file = r.getString(2);
			List<String> list = map.computeIfAbsent(file, k -> new ArrayList<>());
			list.add(id);
			return true;
		});
		return map;
	}
}
