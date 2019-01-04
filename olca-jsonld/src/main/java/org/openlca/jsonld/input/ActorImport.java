package org.openlca.jsonld.input;

import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

class ActorImport extends BaseImport<Actor> {

	private ActorImport(String refId, ImportConfig conf) {
		super(ModelType.ACTOR, refId, conf);
	}

	static Actor run(String refId, ImportConfig conf) {
		return new ActorImport(refId, conf).run();
	}

	@Override
	Actor map(JsonObject json, long id) {
		if (json == null)
			return null;
		Actor a = new Actor();
		In.mapAtts(json, a, id, conf);
		mapAtts(json, a);
		return conf.db.put(a);
	}

	private void mapAtts(JsonObject json, Actor a) {
		a.address = Json.getString(json, "address");
		a.city = Json.getString(json, "city");
		a.country = Json.getString(json, "country");
		a.email = Json.getString(json, "email");
		a.telefax = Json.getString(json, "telefax");
		a.telephone = Json.getString(json, "telephone");
		a.website = Json.getString(json, "website");
		a.zipCode = Json.getString(json, "zipCode");
	}
}
