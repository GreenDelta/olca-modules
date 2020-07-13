package org.openlca.jsonld.input;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

abstract class BaseImport<T extends RootEntity> {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ModelType modelType;
	String refId;
	ImportConfig conf;

	BaseImport(ModelType modelType, String refId, ImportConfig conf) {
		this.refId = refId;
		this.conf = conf;
		this.modelType = modelType;
	}

	final T run() {
		if (refId == null || conf == null)
			return null;
		try {
			T model = get(refId);
			JsonObject json = conf.store.get(modelType, refId);
			if (!doImport(model, json))
				return model;
			importBinFiles();
			conf.visited(modelType, refId);
			model = map(json, model);
			conf.imported(model);
			return model;
		} catch (Exception e) {
			log.error("failed to import " + modelType.name() + " " + refId, e);
			return null;
		}
	}

	private boolean doImport(T model, JsonObject json) {
		if (model == null)
			return true;
		if (json == null || conf.updateMode == UpdateMode.NEVER)
			return false;
		if (conf.updateMode == UpdateMode.ALWAYS)
			return !conf.hasVisited(modelType, refId);
		long jsonVersion = In.getVersion(json);
		long jsonDate = In.getLastChange(json);
		if (jsonVersion < model.version)
			return false;
		return jsonVersion != model.version
				|| jsonDate > model.lastChange;
	}

	/**
	 * This method is overwritten in the `CategoryImport` as the reference ID for
	 * categories may change in the import: the reference ID of a category in
	 * openLCA is calculated from the category type and path. There is a mapping
	 * between the JSON-LD IDs and the reference IDs in the database for categories.
	 */
	protected T get(String refId) {
		return conf.db.get(modelType, refId);
	}

	private void importBinFiles() {
		IDatabase db = conf.db.getDatabase();
		if (db == null || db.getFileStorageLocation() == null)
			return;
		FileStore fs = new FileStore(db.getFileStorageLocation());
		try {
			File dir = fs.getFolder(modelType, refId);
			for (String path : conf.store.getBinFiles(modelType, refId)) {
				byte[] data = conf.store.get(path);
				if (data == null)
					return;
				String fileName = Paths.get(path).getFileName().toString();
				if (!dir.exists())
					dir.mkdirs();
				File file = new File(dir, fileName);
				Files.write(file.toPath(), data, StandardOpenOption.CREATE);
			}
		} catch (Exception e) {
			log.error("failed to import bin files for " + modelType + ":"
					+ refId, e);
		}
	}

	T map(JsonObject json, T model) {
		if (model == null)
			return map(json, 0L);
		return map(json, model.id);
	}

	abstract T map(JsonObject json, long id);

}
