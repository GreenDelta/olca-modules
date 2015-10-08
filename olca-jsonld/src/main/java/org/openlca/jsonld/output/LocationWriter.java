package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.Location;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonObject;

class LocationWriter extends Writer<Location> {

	@Override
	public JsonObject write(Location location, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(location, refFn);
		if (obj == null)
			return null;
		obj.addProperty("code", location.getCode());
		obj.addProperty("latitude", location.getLatitude());
		obj.addProperty("longitude", location.getLongitude());
		// TODO: add kml
		return obj;
	}
}
