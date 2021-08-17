package org.openlca.geo.geojson;

import com.google.gson.JsonObject;
import org.openlca.core.model.Copyable;

public abstract class Geometry implements Copyable<Geometry> {

	public abstract JsonObject toJson();

	@Override
	public abstract Geometry copy();
}
