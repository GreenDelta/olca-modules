package org.openlca.jsonld.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
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

		// version & last change
		re.version = getVersion(obj);
    re.lastChange = getLastChange(obj);

	  // category
    var path = Json.getString(obj, "category");
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

	static long getVersion(JsonObject obj) {
		if (obj == null)
			return 0;
		String version = Json.getString(obj, "version");
		if (version != null)
			return Version.fromString(version).getValue();
		else
			return 0;
	}

	static long getLastChange(JsonObject obj) {
		if (obj == null)
			return 0;
		var date = Json.getDate(obj, "lastChange");
		return date == null
			? 0
			: date.getTime();
	}
}
