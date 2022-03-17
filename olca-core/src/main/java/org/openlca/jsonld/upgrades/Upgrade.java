package org.openlca.jsonld.upgrades;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.JsonStoreReader;

import java.util.List;

/**
 * An upgrade updates the objects in a JSON store of on or more versions to a
 * specific target version. By default, it just delegates the calls to a wrapped
 * reader. Note that it is important to delegate all method calls to the wrapped
 * reader as the reader may overwrite specific default methods of the JSON
 * reader interface.
 */
abstract class Upgrade implements JsonStoreReader {

	protected final JsonStoreReader reader;

	Upgrade (JsonStoreReader reader) {
		this.reader = reader;
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		return reader.getRefIds(type);
	}

	@Override
	public List<String> getFiles(String dir) {
		return reader.getFiles(dir);
	}

	@Override
	public List<String> getBinFiles(ModelType type, String refId) {
		return reader.getBinFiles(type, refId);
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		return reader.get(type, refId);
	}

	@Override
	public List<JsonObject> getAll(ModelType type) {
		return reader.getAll(type);
	}

	@Override
	public JsonElement getJson(String path) {
		return reader.getJson(path);
	}

	@Override
	public byte[] getBytes(String path) {
		return reader.getBytes(path);
	}
}
