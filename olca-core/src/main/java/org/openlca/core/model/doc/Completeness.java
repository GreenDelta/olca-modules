package org.openlca.core.model.doc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.RootEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Documents completeness aspects of a dataset. Values of this class are
 * currently stored in the {@code otherProperties} extension of a dataset.
 */
public class Completeness {

	private final Map<String, String> map = new HashMap<>();

	public void put(String aspect, String value) {
		map.put(aspect, value);
	}

	public String get(String aspect) {
		return map.get(aspect);
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public JsonArray toJson() {
		return Util.toJson(map);
	}

	public void writeTo(RootEntity e) {
		if (e == null)
			return;
		var props = e.readOtherProperties();
		if (props == null) {
			props = new JsonObject();
		}
		props.add("completeness", toJson());
		e.writeOtherProperties(props);
	}

	public static Completeness fromJson(JsonElement e) {
		var c = new Completeness();
		var map = Util.parseMap(e);
		c.map.putAll(map);
		return c;
	}

	public static Completeness readFrom(RootEntity e) {
		if (e == null)
			return new Completeness();
		var props = e.readOtherProperties();
		if (props == null)
			return new Completeness();
		var elem = props.get("completeness");
		return fromJson(elem);
	}
}
