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

	void putBin(ModelType type, String refId, String filename, byte[] data);

	void put(String path, byte[] data);

	byte[] get(String path);
	
	void putContext();

	JsonObject getContext();

	/**
	 * Returns a list of paths to external (binary) files for a model with the
	 * given type and ID. The returned paths should be directly resolvable so
	 * that a call get(path) on this entity store returns the binary data of
	 * this file. If there are no external files available an empty list should
	 * be returned.
	 */
	List<String> getBinFiles(ModelType type, String refId);

}
