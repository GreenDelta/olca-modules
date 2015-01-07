package org.openlca.jsonld;

import java.io.Closeable;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;

public interface EntityStore extends Closeable {

	void add(ModelType type, String refId, JsonObject object);

	boolean contains(ModelType type, String refId);

	/**
	 * Initializes a JSON object for storing an entity. Depending on the store
	 * this object can be initialized with the linked data context (@context)
	 * and other attributes.
	 */
	JsonObject initJson();

}
