package org.openlca.jsonld.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

class Util {

  private Util() {
  }

  static void mapBase(RootEntity e, JsonObject obj, EntityResolver resolver) {

    e.refId = Json.getString(obj, "@id");
    e.name = Json.getString(obj, "name");
    e.description = Json.getString(obj, "description");

    // version
    var version = Json.getString(obj, "version");
    e.version = version != null
      ? Version.fromString(version).getValue()
      : 0L;

    // last change
    var lastChange = Json.getDate(obj, "lastChange");
    e.lastChange = lastChange != null
      ? lastChange.getTime()
      : 0L;

    if (!(e instanceof CategorizedEntity ce))
      return;

	  // category
    var catId = Json.getRefId(obj, "category");
    if (catId != null) {
      ce.category = resolver.get(Category.class, catId);
    }

    // tags
    var tagArray = Json.getArray(obj, "tags");
    if (tagArray != null) {
      var tags = Json.stream(tagArray)
        .filter(JsonElement::isJsonPrimitive)
        .map(JsonElement::getAsString)
        .filter(tag -> !Strings.nullOrEmpty(tag))
        .toArray(String[]::new);
      ce.tags = tags.length > 0
        ? String.join(",", tags)
        : null;
    }

		// library
		ce.library = Json.getString(obj, "library");
  }
}
