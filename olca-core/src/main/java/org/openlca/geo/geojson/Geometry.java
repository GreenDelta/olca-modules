package org.openlca.geo.geojson;

import org.openlca.commons.Copyable;

import com.google.gson.JsonObject;

public abstract class Geometry implements Copyable<Geometry> {

	public abstract JsonObject toJson();

	@Override
	public abstract Geometry copy();
}
