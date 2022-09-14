package org.openlca.jsonld.output;

import org.openlca.core.model.RefEntity;

import com.google.gson.JsonObject;

/**
 * Converts an entity to a JSON object.
 */
public interface JsonWriter<T extends RefEntity> {

	JsonObject write(T entity);

}
