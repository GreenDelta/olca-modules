package org.openlca.core.model.doc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.Copyable;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Contains key-value pairs for documentation aspects.
 */
public class AspectMap implements Copyable<AspectMap> {

	private final HashMap<String, String> map;

	private AspectMap(int capacity) {
		map = new HashMap<>(capacity);
	}

	public AspectMap() {
		this(5);
	}

	public static AspectMap fromJsonBytes(byte[] bytes) {
		if (bytes == null || bytes.length == 0)
			return new AspectMap();
		try (var stream = new ByteArrayInputStream(bytes);
				 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			var json = new Gson().fromJson(reader, JsonArray.class);
			return fromJson(json);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(AspectMap.class);
			log.error("failed to parse aspect map from bytes", e);
			return new AspectMap();
		}
	}

	public static AspectMap fromJson(JsonElement e) {
		if (e == null || !e.isJsonArray())
			return new AspectMap();
		var array = e.getAsJsonArray();
		var aspects = new AspectMap(array.size());
		for (var i : array) {
			if (!i.isJsonObject())
				continue;
			var obj = i.getAsJsonObject();
			var key = Json.getString(obj, "aspect");
			var val = Json.getString(obj, "value");
			if (Strings.nullOrEmpty(key) || Strings.nullOrEmpty(val))
				continue;
			aspects.put(key, val);
		}
		return aspects;
	}

	public byte[] toJsonBytes() {
		var s = new Gson().toJson(toJson());
		return s.getBytes(StandardCharsets.UTF_8);
	}

	public JsonArray toJson() {
		var array = new JsonArray(map.size());
		for (var e : map.entrySet()) {
			if (Strings.nullOrEmpty(e.getKey())
					|| Strings.nullOrEmpty(e.getValue()))
				continue;
			var obj = new JsonObject();
			Json.put(obj, "aspect", e.getKey());
			Json.put(obj, "value", e.getValue());
			array.add(obj);
		}
		return array;
	}

	public String get(String aspect) {
		return map.get(aspect);
	}

	public AspectMap remove(String aspect) {
		map.remove(aspect);
		return this;
	}

	public AspectMap put(String aspect, String value) {
		map.put(aspect, value);
		return this;
	}

	public AspectMap putAll(AspectMap other) {
		if (other == null)
			return this;
		map.putAll(other.map);
		return this;
	}

	public Set<String> getAspects() {
		return Collections.unmodifiableSet(map.keySet());
	}

	public void each(BiConsumer<String, String> fn) {
		if (fn == null)
			return;
		for (var e : map.entrySet()) {
			if (e.getKey() == null || e.getValue() == null)
				continue;
			fn.accept(e.getKey(), e.getValue());
		}
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof AspectMap other))
			return false;
		if (this.map.size() != other.map.size())
			return false;
		for (var e : this.map.entrySet()) {
			if (!other.map.containsKey(e.getKey()))
				return false;
			if (!Objects.equals(e.getValue(), other.map.get(e.getKey())))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		for (var e : this.map.entrySet()) {
			if (e.getKey() != null) {
				hash = 31 * hash + e.getKey().hashCode();
			}
			if (e.getValue() != null) {
				hash = 31 * hash + e.getValue().hashCode();
			}
		}
		return hash;
	}

	@Override
	public AspectMap copy() {
		var copy = new AspectMap(this.map.size());
		copy.map.putAll(this.map);
		return copy;
	}
}
