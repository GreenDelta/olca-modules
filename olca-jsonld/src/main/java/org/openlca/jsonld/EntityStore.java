package org.openlca.jsonld;

import java.io.Closeable;

import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;

public interface EntityStore extends Closeable {

	public void add(ModelType type, String refId, JsonObject object);

	public boolean contains(ModelType type, String refId);

}
