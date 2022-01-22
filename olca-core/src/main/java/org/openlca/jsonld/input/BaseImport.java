package org.openlca.jsonld.input;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.openlca.core.database.FileStore;
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

			// first check if we can directly return the model
			// without touching the store
			if (model != null) {
				if (conf.updateMode == UpdateMode.NEVER
						|| conf.hasVisited(modelType, refId))
					return model;
			}
			var json = conf.reader.get(modelType, refId);
			if (json == null)
				return model;
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
		if (model == null
				|| conf.updateMode == UpdateMode.ALWAYS)
			return true;

		// check version and date
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
		var db = conf.db.getDatabase();
		if (db == null || db.getFileStorageLocation() == null)
			return;
		var fs = new FileStore(db.getFileStorageLocation());
		try {
			var dir = fs.getFolder(modelType, refId);
			for (var path : conf.reader.getBinFiles(modelType, refId)) {
				byte[] data = conf.reader.getBytes(path);
				if (data == null)
					return;
				var name = Paths.get(path).getFileName().toString();
				if (!dir.exists()) {
					Files.createDirectories(dir.toPath());
				}
				File file = new File(dir, name);
				Files.write(file.toPath(), data);
			}
		} catch (Exception e) {
			log.error("failed to import bin files for "
								+ modelType + ":" + refId, e);
		}
	}

	T map(JsonObject json, T model) {
		if (model == null)
			return map(json, 0L);
		return map(json, model.id);
	}

	abstract T map(JsonObject json, long id);

}
