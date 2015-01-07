package org.openlca.jsonld;

import org.openlca.core.model.RootEntity;

import com.google.gson.JsonSerializer;

interface Writer<T extends RootEntity> extends JsonSerializer<T> {

	void write(T entity);

}
