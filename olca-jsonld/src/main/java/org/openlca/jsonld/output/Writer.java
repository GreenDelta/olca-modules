package org.openlca.jsonld.output;

import com.google.gson.JsonSerializer;
import org.openlca.core.model.RootEntity;

interface Writer<T extends RootEntity> extends JsonSerializer<T> {

	void write(T entity);

}
