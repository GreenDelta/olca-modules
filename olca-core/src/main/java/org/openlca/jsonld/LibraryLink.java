package org.openlca.jsonld;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.openlca.commons.Strings;
import org.openlca.core.database.IDatabase;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Contains information about a linked library of a JSON package.
 *
 * @param id
 *            the required library ID
 * @param url
 *            an optional URL from which the library can be downloaded.
 */
public record LibraryLink(String id, String url) {

	public static Optional<LibraryLink> parseFrom(JsonElement json) {
		if (json == null)
			return Optional.empty();

		// for backwards compatibility we accept plain library IDs here
		if (json.isJsonPrimitive()) {
			var prim = json.getAsJsonPrimitive();
			if (!prim.isString())
				return Optional.empty();
			var id = prim.getAsString();
			return id.isBlank()
					? Optional.empty()
					: Optional.of(new LibraryLink(id, null));
		}

		if (!json.isJsonObject())
			return Optional.empty();
		var obj = json.getAsJsonObject();
		var id = Json.getString(obj, "id");
		return Strings.isBlank(id)
				? Optional.empty()
				: Optional.of(new LibraryLink(id, Json.getString(obj, "url")));
	}

	public static List<LibraryLink> allOf(IDatabase db) {
		if (db == null)
			return List.of();
		var libraries = db.getLibraries();
		return of(libraries);
	}

	public static List<LibraryLink> of(Collection<String> libraries) {
		return libraries.stream()
				.filter(Strings::isNotBlank)
				.map(id -> new LibraryLink(id, null))
				.toList();
	}

	public JsonObject toJson() {
		var obj = new JsonObject();
		Json.put(obj, "id", id);
		Json.put(obj, "url", url);
		return obj;
	}
}
