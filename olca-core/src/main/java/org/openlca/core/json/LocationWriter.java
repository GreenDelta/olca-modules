package org.openlca.core.json;

import java.lang.reflect.Type;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.openlca.core.model.Location;

class LocationWriter implements JsonSerializer<Location> {

	@Override
	public JsonElement serialize(Location location, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		map(location, obj);
		return obj;
	}

	static void map(Location location, JsonObject obj) {
		if (location == null || obj == null)
			return;
		JsonWriter.addAttributes(location, obj);
		obj.addProperty("code", location.getCode());
		obj.addProperty("latitude", location.getLatitude());
		obj.addProperty("longitude", location.getLongitude());
		// TODO: add kml
	}
}
