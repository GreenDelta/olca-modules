package org.openlca.jsonld;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;

/**
 * Reads JSON objects and linked binary files from some data source.
 */
public interface JsonStoreReader {

	/**
	 * Get the IDs of all data sets of the given type from the underlying data
	 * store.
	 */
	List<String> getRefIds(ModelType type);

	/**
	 * Get the files of the given folder.
	 *
	 * @param dir the folder of which the files should be returned.
	 * @return a list with full paths of the files in the folder so that each
	 * returned path can be resolved using the {@code getBytes(path)} method.
	 */
	List<String> getFiles(String dir);

	/**
	 * Returns a list of paths to linked binary files for a model with the given
	 * type and ID. The returned paths should be directly resolvable so that a
	 * call {@code getBytes(path)} returns the binary data of this file. If there
	 * are no external files available an empty list should be returned.
	 */
	default List<String> getBinFiles(ModelType type, String refId) {
		if (type == null || refId == null)
			return Collections.emptyList();
		var dir = ModelPath.binFolderOf(type, refId);
		return getFiles(dir);
	}

	/**
	 * Get the JSON object of the data set of the given type and ID.
	 */
	default JsonObject get(ModelType type, String refId) {
		var path = ModelPath.jsonOf(type, refId);
		var json = getJson(path);
		return json != null && json.isJsonObject()
			? json.getAsJsonObject()
			: null;
	}

	default List<JsonObject> getAll(ModelType type) {
		return getRefIds(type).stream()
			.map(refId -> get(type, refId))
			.filter(Objects::nonNull)
			.toList();
	}

	/**
	 * Parse the content of the file that is stored under the given path as JSON.
	 */
	default JsonElement getJson(String path) {
		var bytes = getBytes(path);
		if (bytes == null)
			return null;
		var json = new String(bytes, StandardCharsets.UTF_8);
		return new Gson().fromJson(json, JsonElement.class);
	}

	/**
	 * Get the raw bytes of the JSON or binary file that is stored under the
	 * given path.
	 */
	byte[] getBytes(String path);

}
