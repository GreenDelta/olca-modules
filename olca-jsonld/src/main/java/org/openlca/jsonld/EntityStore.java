package org.openlca.jsonld;

import java.io.Closeable;
import java.util.List;

import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;

public interface EntityStore extends Closeable {

	/**
	 * Put the given json object of the given type into the store.
	 */
	void put(ModelType type, JsonObject object);

	boolean contains(ModelType type, String refId);

	List<String> getRefIds(ModelType type);

	JsonObject get(ModelType type, String refId);

	void put(String path, byte[] data);

	byte[] get(String path);

}
