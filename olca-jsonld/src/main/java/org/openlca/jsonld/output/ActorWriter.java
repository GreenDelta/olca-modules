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
		Out.put(obj, "address", actor.getAddress());
		Out.put(obj, "city", actor.getCity());
		Out.put(obj, "country", actor.getCountry());
		Out.put(obj, "email", actor.getEmail());
		Out.put(obj, "telefax", actor.getTelefax());
		Out.put(obj, "telephone", actor.getTelephone());
		Out.put(obj, "website", actor.getWebsite());
		Out.put(obj, "zipCode", actor.getZipCode());
	}
}
