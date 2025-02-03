package org.openlca.io.smartepd;

import java.util.Objects;
import java.util.Optional;

import org.openlca.jsonld.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record SmartRefUnit(JsonObject json) {

	public static final int DECLARED = 1;
	public static final int FUNCTIONAL = 2;

	public SmartRefUnit() {
		this(new JsonObject());
	}

	public SmartRefUnit(JsonObject json) {
		this.json = Objects.requireNonNull(json);
	}

	public static Optional<SmartRefUnit> of(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return Optional.empty();
		var obj = e.getAsJsonObject();
		return Optional.of(new SmartRefUnit(obj));
	}

	public double qty() {
		return Json.getDouble(json, "qty", 0);
	}

	public SmartRefUnit qty(double qty) {
		Json.put(json, "qty", qty);
		return this;
	}

	public String unit() {
		return Json.getString(json, "unit");
	}

	public SmartRefUnit unit(String unit) {
		Json.put(json, "unit", unit);
		return this;
	}

}
