package org.openlca.io.openepd;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

/**
 * A measurement value.
 *
 * @param mean (expected) mean value of the measurement
 * @param unit the measurement unit
 * @param rsd  the relative standard deviation, i.e. standard_deviation/mean
 * @param dist statistical distribution of the measurement error
 */
public record EpdMeasurement(
	Double mean,
	String unit,
	Double rsd,
	String dist
) implements Jsonable {

	public static EpdMeasurement of(double amount, String unit) {
		return new EpdMeasurement(amount, unit, null, null);
	}

	public static Optional<EpdMeasurement> fromJson(JsonElement elem) {
		if (elem == null || !elem.isJsonObject())
			return Optional.empty();
		var obj = elem.getAsJsonObject();
		var mean = Json.getDouble(obj, "mean");
		var rsd = Json.getDouble(obj, "rsd");
		var m = new EpdMeasurement(
			mean.isPresent() ? mean.getAsDouble() : null,
			Json.getString(obj, "unit"),
			rsd.isPresent() ? rsd.getAsDouble() : null,
			Json.getString(obj, "dist")
		);
		return Optional.of(m);
	}

	@Override
	public JsonObject toJson() {
		var obj = new JsonObject();
		Json.put(obj, "mean", mean);
		Json.put(obj, "unit", unit);
		Json.put(obj, "rsd", rsd);
		Json.put(obj, "dist", dist);
		return obj;
	}
}

