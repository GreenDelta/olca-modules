package org.openlca.jsonld.output;

import java.lang.reflect.Type;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.EntityStore;

class ActorWriter implements Writer<Actor> {

	private EntityStore store;

	public ActorWriter() {
	}

	public ActorWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public void write(Actor actor) {
		if (actor == null || store == null)
			return;
		if (store.contains(ModelType.ACTOR, actor.getRefId()))
			return;
		JsonObject obj = serialize(actor, null, null);
		store.add(ModelType.ACTOR, actor.getRefId(), obj);
	}

	@Override
	public JsonObject serialize(Actor actor, Type type,
			JsonSerializationContext context) {
		JsonObject obj = store == null ? new JsonObject() : store.initJson();
		map(actor, obj);
		return obj;
	}

	private void map(Actor actor, JsonObject obj) {
		if (actor == null || obj == null)
			return;
		JsonExport.addAttributes(actor, obj, store);
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
