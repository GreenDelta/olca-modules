package org.openlca.jsonld.output;

import org.openlca.core.model.Actor;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

record ActorWriter(JsonExport exp) implements Writer<Actor> {

	@Override
	public JsonObject write(Actor actor) {
		var obj = Writer.init(actor);
		Json.put(obj, "address", actor.address);
		Json.put(obj, "city", actor.city);
		Json.put(obj, "country", actor.country);
		Json.put(obj, "email", actor.email);
		Json.put(obj, "telefax", actor.telefax);
		Json.put(obj, "telephone", actor.telephone);
		Json.put(obj, "website", actor.website);
		Json.put(obj, "zipCode", actor.zipCode);
		return obj;
	}

}
