package org.openlca.jsonld.output;

import org.openlca.core.model.Actor;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

class ActorWriter extends Writer<Actor> {

	ActorWriter(JsonExport exp) {
		super(exp);
	}

	@Override
	JsonObject write(Actor actor) {
		JsonObject obj = super.write(actor);
		if (obj == null)
			return null;
		map(actor, obj);
		return obj;
	}

	private void map(Actor actor, JsonObject obj) {
		Json.put(obj, "address", actor.address);
		Json.put(obj, "city", actor.city);
		Json.put(obj, "country", actor.country);
		Json.put(obj, "email", actor.email);
		Json.put(obj, "telefax", actor.telefax);
		Json.put(obj, "telephone", actor.telephone);
		Json.put(obj, "website", actor.website);
		Json.put(obj, "zipCode", actor.zipCode);
	}
}
