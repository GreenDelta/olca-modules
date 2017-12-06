package org.openlca.cloud.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openlca.cloud.api.FetchNotifier.TaskType;
import org.openlca.cloud.api.data.ModelStreamReader;
import org.openlca.cloud.model.data.Dataset;
import org.openlca.core.database.CategorizedEntityDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.jsonld.EntityStore;
import org.openlca.jsonld.ModelPath;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.util.BinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class FetchHandler {

	private final static Logger log = LoggerFactory.getLogger(FetchHandler.class);
	private final IDatabase database;
	private final Map<Dataset, JsonObject> mergedData;
	private final FetchNotifier fetchNotifier;

	FetchHandler(IDatabase database, Map<Dataset, JsonObject> mergedData, FetchNotifier fetchNotifier) {
		this.database = database;
		this.mergedData = mergedData;
		this.fetchNotifier = fetchNotifier;
	}

	String handleResponse(InputStream input) {
		File file = null;
		try (ModelStreamReader reader = new ModelStreamReader(input)) {
			String commitId = reader.readNextPartAsString();
			List<Dataset> toDelete = new ArrayList<>();
			file = Files.createTempFile("olca", ".zip").toFile();
			file.delete();
			EntityStore store = ZipStore.open(file);
			int toImport = putMergedData(store);
			fetchNotifier.beginTask(TaskType.FETCH, reader.getTotal());
			while (reader.hasMore()) {
				Dataset delete = handleNext(reader, store);
				if (delete != null) {
					toDelete.add(delete);
				} else {
					toImport++;
				}
				fetchNotifier.worked();
			}
			fetchNotifier.endTask();
			if (mergedData != null) {
				for (Entry<Dataset, JsonObject> entry : mergedData.entrySet()) {
					if (entry.getValue() != null)
						continue;
					toDelete.add(entry.getKey());
				}
			}
			if (toImport == 0 && toDelete.isEmpty())
				return commitId;
			fetchNotifier.beginTask(TaskType.PULL, toImport + toDelete.size());
			if (toImport != 0) {
				JsonImport jsonImport = new JsonImport(store, database);
				jsonImport.setUpdateMode(UpdateMode.ALWAYS);
				jsonImport.setCallback((e) -> fetchNotifier.worked());
				jsonImport.run();
			}
			for (Dataset dataset : toDelete) {
				delete(Daos.categorized(database, dataset.type), dataset.refId);
				fetchNotifier.worked();
			}
			fetchNotifier.endTask();
			return commitId;
		} catch (IOException e) {
			log.error("Error reading fetch data", e);
			return null;
		} finally {
			file.delete();
		}
	}

	private Dataset handleNext(ModelStreamReader reader, EntityStore store) throws IOException {
		Dataset dataset = reader.readNextPartAsDataset();
		byte[] data = reader.readNextPart();
		data = BinUtils.gunzip(data);
		if (data.length == 0)
			return dataset;
		store.put(ModelPath.get(dataset.type, dataset.refId), data);
		int noOfFiles = reader.readNextInt();
		if (noOfFiles == 0)
			return null;
		int count = 0;
		while (count++ < noOfFiles) {
			String path = reader.readNextPartAsString();
			data = reader.readNextPart();
			store.putBin(dataset.type, dataset.refId, path, data);
		}
		return null;
	}

	private int putMergedData(EntityStore store) {
		if (mergedData == null)
			return 0;
		int toImport = 0;
		for (Entry<Dataset, JsonObject> entry : mergedData.entrySet()) {
			JsonObject json = entry.getValue();
			if (json == null)
				continue;
			store.put(entry.getKey().type, json);
			toImport++;
			if (isImpactMethod(json)) {
				putReferences(json, "impactCategories", ModelType.IMPACT_CATEGORY, store);
				putReferences(json, "nwSets", ModelType.NW_SET, store);
			}
		}
		return toImport;
	}

	private void putReferences(JsonObject json, String field, ModelType type, EntityStore store) {
		if (!json.has(field))
			return;
		JsonArray array = json.getAsJsonArray(field);
		for (JsonElement element : array) {
			if (!element.isJsonObject())
				continue;
			store.put(type, element.getAsJsonObject());
		}
	}

	private boolean isImpactMethod(JsonObject json) {
		String type = json.get("@type").getAsString();
		return ImpactMethod.class.getSimpleName().equals(type);
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> void delete(
			CategorizedEntityDao<T, V> dao, String refId) {
		if (!dao.contains(refId))
			return;
		dao.delete(dao.getForRefId(refId));
	}

}
