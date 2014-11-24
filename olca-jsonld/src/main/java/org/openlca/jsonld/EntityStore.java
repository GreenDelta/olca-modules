package org.openlca.jsonld;

import java.io.Closeable;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;

interface EntityStore extends Closeable {

	public void add(ModelType type, JsonObject object);

	public boolean contains(ModelType type, String refId);

}
