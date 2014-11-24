package org.openlca.jsonld;

import com.google.gson.JsonSerializer;
import org.openlca.core.model.RootEntity;

interface Writer<T extends RootEntity> extends JsonSerializer<T> {

	/**
	 * Writes the given entity and its dependencies to the given store if it is
	 * not yet contained.
	 */
	void write(T entity, EntityStore store);

}
