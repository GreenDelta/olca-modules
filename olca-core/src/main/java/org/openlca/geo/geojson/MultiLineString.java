package org.openlca.geo.geojson;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

public final class MultiLineString extends Geometry {

	public final List<LineString> lineStrings;

	MultiLineString(List<LineString> lines) {
		this.lineStrings = lines;
	}

	public MultiLineString() {
		this(new ArrayList<>());
	}

	public static MultiLineString fromJson(JsonObject obj) {
		List<LineString> lines = Coordinates.readLines(
				obj.get("coordinates"));
		return new MultiLineString(lines);
	}

	@Override
	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "MultiLineString");
		obj.add("coordinates",
				Coordinates.writeLines(lineStrings));
		return obj;
	}

	@Override
	public MultiLineString copy() {
		MultiLineString c = new MultiLineString();
		if (lineStrings == null)
			return c;
		for (LineString line : lineStrings) {
			if (line == null)
				continue;
			c.lineStrings.add(line.copy());
		}
		return c;
	}

}
