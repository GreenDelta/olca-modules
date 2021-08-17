package org.openlca.geo.geojson;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.Copyable;

public final class FeatureCollection implements Copyable<FeatureCollection> {

	public final List<Feature> features = new ArrayList<>();

	public static FeatureCollection empty() {
		return new FeatureCollection();
	}

	public static FeatureCollection of(Feature f) {
		var coll = new FeatureCollection();
		if (f != null) {
			coll.features.add(f);
		}
		return coll;
	}

	public static FeatureCollection of(Geometry g) {
		var coll = new FeatureCollection();
		if (g != null) {
			Feature f = new Feature();
			f.geometry = g;
			coll.features.add(f);
		}
		return coll;
	}

	public static FeatureCollection fromJson(JsonObject obj) {
		FeatureCollection coll = new FeatureCollection();
		JsonElement elem = obj.get("features");
		if (elem == null || !elem.isJsonArray())
			return coll;
		for (JsonElement f : elem.getAsJsonArray()) {
			if (f == null || !f.isJsonObject())
				continue;
			Feature feature = Feature.fromJson(
					f.getAsJsonObject());
			coll.features.add(feature);
		}
		return coll;
	}

	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "FeatureCollection");
		JsonArray array = new JsonArray();
		for (Feature f : features) {
			if (f == null)
				continue;
			array.add(f.toJson());
		}
		obj.add("features", array);
		return obj;
	}

	public Feature first() {
		return features.isEmpty() ? null : features.get(0);
	}

	public boolean isEmpty() {
		return features.isEmpty();
	}

	@Override
	public FeatureCollection copy() {
		FeatureCollection c = new FeatureCollection();
		for (Feature f : features) {
			if (f == null)
				continue;
			c.features.add(f.copy());
		}
		return c;
	}

}
