package org.openlca.jsonld;

import java.io.Closeable;
import java.util.List;

import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;

public interface EntityStore extends Closeable {

	void put(ModelType type, JsonObject object);

	boolean contains(ModelType type, String refId);

	/**
	 * Initializes a JSON object for storing an entity. Depending on the store
	 * this object can be initialized with the linked data context (@context)
	 * and other attributes.
	 */
	JsonObject initJson();

	List<String> getRefIds(ModelType type);

	JsonObject get(ModelType type, String refId);

}
