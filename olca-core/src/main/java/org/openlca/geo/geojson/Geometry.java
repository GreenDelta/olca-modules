package org.openlca.geo.geojson;

import com.google.gson.JsonObject;

public abstract class Geometry implements Cloneable {

	public abstract JsonObject toJson();

	@Override
	public abstract Geometry clone();
}
