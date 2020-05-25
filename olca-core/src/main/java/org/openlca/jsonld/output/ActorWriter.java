package org.openlca.jsonld.output;

import org.openlca.core.model.Actor;

import com.google.gson.JsonObject;

class ActorWriter extends Writer<Actor> {

	ActorWriter(ExportConfig conf) {
		super(conf);
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
		Out.put(obj, "address", actor.address);
		Out.put(obj, "city", actor.city);
		Out.put(obj, "country", actor.country);
		Out.put(obj, "email", actor.email);
		Out.put(obj, "telefax", actor.telefax);
		Out.put(obj, "telephone", actor.telephone);
		Out.put(obj, "website", actor.website);
		Out.put(obj, "zipCode", actor.zipCode);
	}
}
