package org.openlca.jsonld.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Category;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

class Util {

  private Util() {
  }

  static void mapBase(RefEntity e, JsonObject obj, EntityResolver resolver) {

    e.refId = Json.getString(obj, "@id");
    e.name = Json.getString(obj, "name");
    e.description = Json.getString(obj, "description");

    if (!(e instanceof RootEntity re))
      return;

    // version
    var version = Json.getString(obj, "version");
    re.version = version != null
      ? Version.fromString(version).getValue()
      : 0L;

    // last change
    var lastChange = Json.getDate(obj, "lastChange");
    re.lastChange = lastChange != null
      ? lastChange.getTime()
      : 0L;

	  // category
    var path = Json.getRefId(obj, "category");
    if (path != null) {
			var type = ModelType.of(re);
      re.category = resolver.getCategory(type, path);
    }

    // tags
    var tagArray = Json.getArray(obj, "tags");
    if (tagArray != null) {
      var tags = Json.stream(tagArray)
        .filter(JsonElement::isJsonPrimitive)
        .map(JsonElement::getAsString)
        .filter(tag -> !Strings.nullOrEmpty(tag))
        .toArray(String[]::new);
      re.tags = tags.length > 0
        ? String.join(",", tags)
        : null;
    }
  }
}
