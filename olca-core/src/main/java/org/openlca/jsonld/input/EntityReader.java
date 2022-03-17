package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import org.openlca.core.model.RootEntity;

public interface EntityReader<T extends RootEntity> {

  T read(JsonObject json);

  void update(T entity, JsonObject json);

}
