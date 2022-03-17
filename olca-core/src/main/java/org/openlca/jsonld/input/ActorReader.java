package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.model.Actor;
import org.openlca.core.io.EntityResolver;
import org.openlca.jsonld.Json;

public record ActorReader(EntityResolver resolver)
	implements EntityReader<Actor> {

	public ActorReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public Actor read(JsonObject json) {
		var actor = new Actor();
		update(actor, json);
		return actor;
	}

	@Override
	public void update(Actor actor, JsonObject json) {
		Util.mapBase(actor, json, resolver);
		actor.address = Json.getString(json, "address");
		actor.city = Json.getString(json, "city");
		actor.country = Json.getString(json, "country");
		actor.email = Json.getString(json, "email");
		actor.telefax = Json.getString(json, "telefax");
		actor.telephone = Json.getString(json, "telephone");
		actor.website = Json.getString(json, "website");
		actor.zipCode = Json.getString(json, "zipCode");
	}

}
