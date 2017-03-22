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

	private Logger log = LoggerFactory.getLogger(getClass());
	private ModelType modelType;
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
		if (json == null)
			return false;
		if (conf.updateMode == UpdateMode.ALWAYS)
			return !conf.hasVisited(modelType, refId);
		long jsonVersion = In.getVersion(json);
		long jsonDate = In.getLastChange(json);
		if (jsonVersion < model.getVersion())
			return false;
		if (jsonVersion == model.getVersion()
				&& jsonDate <= model.getLastChange())
			return false;
		return true;
	}

	@SuppressWarnings("unchecked")
	protected T get(String refId) {
		switch (modelType) {
		case ACTOR:
			return (T) conf.db.getActor(refId);
		case CATEGORY:
			return (T) conf.db.getCategory(refId);
		case CURRENCY:
			return (T) conf.db.getCurrency(refId);
		case FLOW:
			return (T) conf.db.getFlow(refId);
		case FLOW_PROPERTY:
			return (T) conf.db.getFlowProperty(refId);
		case IMPACT_METHOD:
			return (T) conf.db.getMethod(refId);
		case LOCATION:
			return (T) conf.db.getLocation(refId);
		case PARAMETER:
			return (T) conf.db.getParameter(refId);
		case PROCESS:
			return (T) conf.db.getProcess(refId);
		case SOCIAL_INDICATOR:
			return (T) conf.db.getSocialIndicator(refId);
		case SOURCE:
			return (T) conf.db.getSource(refId);
		case UNIT_GROUP:
			return (T) conf.db.getUnitGroup(refId);
		case PRODUCT_SYSTEM:
			return (T) conf.db.getSystem(refId);
		case PROJECT:
			return (T) conf.db.getProject(refId);
		case DQ_SYSTEM:
			return (T) conf.db.getDqSystem(refId);
		default:
			throw new RuntimeException(modelType.name() + " not supported");
		}
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
			return map(json, 0l);
		return map(json, model.getId());
	}

	abstract T map(JsonObject json, long id);

}
