package org.openlca.jsonld.input;

import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;

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
		a.setAddress(In.getString(json, "address"));
		a.setCity(In.getString(json, "city"));
		a.setCountry(In.getString(json, "country"));
		a.setEmail(In.getString(json, "email"));
		a.setTelefax(In.getString(json, "telefax"));
		a.setTelephone(In.getString(json, "telephone"));
		a.setWebsite(In.getString(json, "website"));
		a.setZipCode(In.getString(json, "zipCode"));
	}
}
