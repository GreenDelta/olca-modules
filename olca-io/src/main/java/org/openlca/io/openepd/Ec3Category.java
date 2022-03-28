package org.openlca.io.openepd;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

public class Ec3Category implements Jsonable {

	public String id;
	public String name;
	public String description;

	/**
	 * The full path of the category in openEPD format.
	 */
	public String openEpd;

	public final List<String> parents = new ArrayList<>();
	public final List<Ec3Category> subCategories = new ArrayList<>();

	public static Optional<Ec3Category> fromJson(JsonElement elem) {
		if (elem == null || !elem.isJsonObject())
			return Optional.empty();
		var obj = elem.getAsJsonObject();
		var category = new Ec3Category();
		category.id = Json.getString(obj, "id");
		category.name = Json.getString(obj, "display_name");
		if (category.name == null) {
			category.name = Json.getString(obj, "name");
		}
		category.description = Json.getString(obj, "description");
		category.openEpd = Json.getString(obj, "openepd");

		Json.stream(Json.getArray(obj, "parents"))
			.filter(JsonElement::isJsonPrimitive)
			.map(JsonElement::getAsString)
			.forEach(category.parents::add);

		Json.stream(Json.getArray(obj, "subcategories"))
			.filter(JsonElement::isJsonObject)
			.map(Ec3Category::fromJson)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.forEach(category.subCategories::add);

		return Optional.of(category);
	}

	@Override
	public JsonObject toJson() {
		var obj = new JsonObject();
		Json.put(obj, "id", id);
		Json.put(obj, "name", name);
		Json.put(obj, "description", description);
		Json.put(obj, "openepd", openEpd);

		if (!parents.isEmpty()) {
			var parentsArray = new JsonArray();
			for (var p : parents) {
				parentsArray.add(p);
			}
			obj.add("parents", parentsArray);
		}

		if (!subCategories.isEmpty()) {
			var catArray = new JsonArray();
			for (var c : subCategories) {
				catArray.add(c.toJson());
			}
			obj.add("subcategories", catArray);
		}
		return obj;
	}
}
