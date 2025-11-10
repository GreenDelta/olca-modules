package org.openlca.git.actions;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.openlca.commons.Strings;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.git.repo.OlcaRepository;
import org.openlca.util.KeyGen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

class Categories {

	private static final Gson gson = new Gson();
	private final Map<String, String> refIdToName = new HashMap<>();
	private final Map<String, ModelType> refIdToType = new HashMap<>();
	private final Map<String, String> refIdToParent = new HashMap<>();
	private final Map<String, String> pathToRefId = new HashMap<>();

	static Categories of(OlcaRepository repo, String commitId) {
		return new Categories(repo, commitId);
	}

	private Categories(OlcaRepository repo, String commitId) {
		init(repo, commitId, "");
	}

	private void init(OlcaRepository repo, String commitId, String path) {
		repo.references.find().includeCategories().commit(commitId).iterate(entry -> {
			if (!entry.isCategory)
				return;
			var refId = getRefId(entry.path);
			refIdToName.put(refId, entry.name);
			refIdToType.put(refId, entry.type);
			if (Strings.isNotBlank(entry.category)) {
				refIdToParent.put(refId, getRefId(entry.type.name() + "/" + entry.category));
			}
			pathToRefId.put(entry.path, refId);
		});
	}

	private String getRefId(String path) {
		return KeyGen.get(path.split("/"));
	}

	byte[] getForPath(String path) {
		var json = getForRefId(pathToRefId.get(path));
		if (json == null)
			return null;
		try {
			return gson.toJson(json).getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	JsonObject getForRefId(String refId) {
		if (refId == null)
			return null;
		JsonObject category = new JsonObject();
		category.addProperty("@type", ModelType.CATEGORY.name());
		category.addProperty("@id", refId);
		category.addProperty("name", refIdToName.get(refId));
		category.addProperty("modelType", refIdToType.get(refId).name());
		category.addProperty("version", new Version(0).toString());
		var parentRefId = refIdToParent.get(refId);
		if (parentRefId != null) {
			var parent = new JsonObject();
			parent.addProperty("@type", ModelType.CATEGORY.name());
			parent.addProperty("@id", parentRefId);
			category.add("category", parent);
		}
		return category;
	}

}
