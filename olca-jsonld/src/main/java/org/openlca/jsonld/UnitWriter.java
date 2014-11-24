package org.openlca.jsonld;

import java.lang.reflect.Type;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.openlca.core.model.Unit;

class UnitWriter implements JsonSerializer<Unit> {

	@Override
	public JsonElement serialize(Unit unit, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		map(unit, obj);
		return obj;
	}

	static void map(Unit unit, JsonObject object) {
		if (unit == null || object == null)
			return;
		JsonWriter.addAttributes(unit, object);
		object.addProperty("conversionFactor", unit.getConversionFactor());
		addSynonyms(unit, object);
	}

	private static void addSynonyms(Unit unit, JsonObject object) {
		String synonyms = unit.getSynonyms();
		if (synonyms == null || synonyms.trim().isEmpty())
			return;
		JsonArray array = new JsonArray();
		String[] items = synonyms.split(";");
		for (String item : items)
			array.add(new JsonPrimitive(item.trim()));
		object.add("synonyms", array);
	}

}
