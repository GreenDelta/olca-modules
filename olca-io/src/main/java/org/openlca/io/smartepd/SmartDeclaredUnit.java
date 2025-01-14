package org.openlca.io.smartepd;

import java.util.Objects;
import java.util.Optional;

import org.openlca.jsonld.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record SmartDeclaredUnit(JsonObject json) {

	public SmartDeclaredUnit() {
		this(new JsonObject());
	}

	public SmartDeclaredUnit(JsonObject json) {
		this.json = Objects.requireNonNull(json);
	}

	public static Optional<SmartDeclaredUnit> of(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return Optional.empty();
		var obj = e.getAsJsonObject();
		return Optional.of(new SmartDeclaredUnit(obj));
	}

	public double qty() {
		return Json.getDouble(json, "qty", 0);
	}

	public SmartDeclaredUnit qty(double qty) {
		Json.put(json, "qty", qty);
		return this;
	}

	public String unit() {
		return Json.getString(json, "unit");
	}

	public SmartDeclaredUnit unit(String unit) {
		Json.put(json, "unit", unit);
		return this;
	}

}
