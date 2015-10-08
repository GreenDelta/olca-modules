package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.Actor;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonObject;

class ActorWriter extends Writer<Actor> {

	@Override
	JsonObject write(Actor actor, Consumer<RootEntity> refHandler) {
		JsonObject obj = super.write(actor, refHandler);
		if (obj == null)
			return null;
		map(actor, obj);
		return obj;
	}

	private void map(Actor actor, JsonObject obj) {
		if (actor == null || obj == null)
			return;
		obj.addProperty("address", actor.getAddress());
		obj.addProperty("city", actor.getCity());
		obj.addProperty("country", actor.getCountry());
		obj.addProperty("email", actor.getEmail());
		obj.addProperty("telefax", actor.getTelefax());
		obj.addProperty("telephone", actor.getTelephone());
		obj.addProperty("website", actor.getWebsite());
		obj.addProperty("zipCode", actor.getZipCode());
	}
}
