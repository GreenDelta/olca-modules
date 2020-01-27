package org.openlca.geo.geojson;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

public final class FeatureCollection {

	public final List<Feature> features = new ArrayList<>();

	static FeatureCollection of(Feature f) {
		FeatureCollection coll = new FeatureCollection();
		if (f != null) {
			coll.features.add(f);
		}
		return coll;
	}

	static FeatureCollection of(Geometry g) {
		FeatureCollection coll = new FeatureCollection();
		if (g != null) {
			Feature f = new Feature();
			f.geometry = g;
			coll.features.add(f);
		}
		return coll;
	}

	static FeatureCollection fromJson(JsonObject obj) {
		FeatureCollection coll = new FeatureCollection();
		// TODO: read features
		return coll;
	}

}
