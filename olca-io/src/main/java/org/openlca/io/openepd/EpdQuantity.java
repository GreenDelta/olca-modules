package org.openlca.io.openepd;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

public record EpdQuantity(double amount, String unit) implements Jsonable {

	public static EpdQuantity of(double amount, String unit) {
		return new EpdQuantity(amount, unit);
	}

	public static Optional<EpdQuantity> fromJson(JsonElement elem) {
		if (elem == null || !elem.isJsonObject())
			return Optional.empty();
		var obj = elem.getAsJsonObject();
		var unit = Json.getString(obj, "unit");
		var qty = new EpdQuantity(
			Json.getDouble(obj, "qty", 0),
			unit != null ? unit : ""
		);
		return Optional.of(qty);
	}

	@Override
	public JsonObject toJson() {
		var obj = new JsonObject();
		obj.addProperty("qty", amount);
		obj.addProperty("unit", unit);
		return obj;
	}

	@Override
	public String toString() {
		return amount + " " + unit;
	}
}
