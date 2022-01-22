package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import org.openlca.core.model.CategorizedEntity;

public interface EntityReader<T extends CategorizedEntity> {

  T read(JsonObject json);

  void update(T entity, JsonObject json);

}
