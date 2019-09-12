package org.openlca.core.database.upgrades;

import java.io.File;
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

class Upgrade4Files {

	private Logger log = LoggerFactory.getLogger(getClass());
	private File root;
	private FileStore store;
	private IDatabase db;

	private Upgrade4Files(File root, IDatabase db) {
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
		new Upgrade4Files(root, db).exec();
	}

	private void exec() {
		log.info("update file store {} of database {}", root, db);
		copyShapeFiles();
		copyLayoutFiles();
		copySourceDocs();
	}

	private void copyShapeFiles() {
		File shapeFileDir = new File(root, "shapefiles");
		if (!shapeFileDir.exists())
			return;
		for (File sourceDir : shapeFileDir.listFiles()) {
			if (!sourceDir.isDirectory())
				continue;
			String id = sourceDir.getName();
			File targetDir = store.getFolder(ModelType.IMPACT_METHOD, id);
			if (!targetDir.exists())
				targetDir.mkdirs();
			for (File source : sourceDir.listFiles()) {
				File target = new File(targetDir, source.getName());
				copyFile(source, target);
			}
		}
	}

	private void copyLayoutFiles() {
		File layoutDir = new File(root, "layouts");
		if (!layoutDir.exists())
			return;
		for (File file : layoutDir.listFiles()) {
			String name = file.getName(); // name = <id>.json
			String id = name.substring(0, name.length() - 5);
			File dir = store.getFolder(ModelType.PRODUCT_SYSTEM, id);
			if (!dir.exists())
				dir.mkdirs();
			copyFile(file, new File(dir, "layout.json"));
		}
	}

	private void copySourceDocs() {
		File docDir = new File(root, "external_docs");
		if (!docDir.exists())
			return;
		Map<String, List<String>> map = getSourceDocs();
		for (File file : docDir.listFiles()) {
			String name = file.getName();
			List<String> sourceIds = map.get(name);
			if (sourceIds == null)
				continue;
			for (String id : sourceIds) {
				File dir = store.getFolder(ModelType.SOURCE, id);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				copyFile(file, new File(dir, name));
			}
		}
	}

	private void copyFile(File source, File target) {
		if (target.exists())
			return;
		try {
			Files.copy(source.toPath(), target.toPath());
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to copy " + source + " to " + target, e);
		}
	}

	private Map<String, List<String>> getSourceDocs() {
		Map<String, List<String>> map = new HashMap<>();
		String query = "select ref_id, external_file from tbl_sources";
		try {
			NativeSql.on(db).query(query, r -> {
				String id = r.getString(1);
				String file = r.getString(2);
				List<String> list = map.get(file);
				if (list == null) {
					list = new ArrayList<>();
					map.put(file, list);
				}
				list.add(id);
				return true;
			});
			return map;
		} catch (Exception e) {
			throw new RuntimeException("failed to get source docs " + query, e);
		}
	}
}
