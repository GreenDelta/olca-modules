package org.openlca.jsonld;

import java.lang.reflect.Type;

import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

class LocationWriter implements Writer<Location> {

	private EntityStore store;

	public LocationWriter() {
	}

	public LocationWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public void write(Location location) {
		if (location == null || store == null)
			return;
		if (store.contains(ModelType.LOCATION, location.getRefId()))
			return;
		JsonObject obj = serialize(location, null, null);
		store.add(ModelType.LOCATION, location.getRefId(), obj);
	}

	@Override
	public void skipContext() {

	}

	@Override
	public JsonObject serialize(Location location, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		map(location, obj);
		return obj;
	}

	private void map(Location location, JsonObject obj) {
		if (location == null || obj == null)
			return;
		JsonWriter.addAttributes(location, obj, store);
		obj.addProperty("code", location.getCode());
		obj.addProperty("latitude", location.getLatitude());
		obj.addProperty("longitude", location.getLongitude());
		// TODO: add kml
	}

}
