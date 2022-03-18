package org.openlca.jsonld.output;

import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Unit;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

record UnitWriter(JsonExport exp) implements Writer<Unit> {

	@Override
	public JsonObject write(Unit unit) {
		var json = new JsonObject();
		map(unit, json);
		tagRefUnit(unit, json);
		return json;
	}

	private void tagRefUnit(Unit unit, JsonObject obj) {
		if (unit == null || obj == null)
			return;
		if (unit.id == 0 || unit.conversionFactor != 1)
			return;
		var sql = "select count(*) from tbl_unit_groups " +
				"where f_reference_unit = " + unit.id;
		try {
			NativeSql.on(exp.db).query(sql, r -> {
				var count = r.getInt(1);
				if (count > 0) {
					obj.addProperty("referenceUnit", true);
				}
				return false;
			});
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to execute query: " + sql, e);
		}
	}

	static void map(Unit unit, JsonObject obj) {
		if (unit == null || obj == null)
			return;
		Writer.mapBasicAttributes(unit, obj);
		Json.put(obj, "conversionFactor", unit.conversionFactor);
		var synonyms = unit.synonyms;
		if (Strings.nullOrEmpty(synonyms))
			return;
		var array = new JsonArray();
		Arrays.stream(synonyms.split(";"))
				.map(String::trim)
				.filter(Strings::notEmpty)
				.map(JsonPrimitive::new)
				.forEach(array::add);
		Json.put(obj, "synonyms", array);
	}
}
