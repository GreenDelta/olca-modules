package org.openlca.jsonld;

import java.lang.reflect.Type;

import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

class ActorWriter implements Writer<Actor> {

	private EntityStore store;
	private boolean writeContext = true;

	public ActorWriter() {
	}

	public ActorWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public void skipContext() {
		this.writeContext = false;
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
		JsonObject obj = new JsonObject();
		if (writeContext)
			JsonWriter.addContext(obj);
		map(actor, obj);
		return obj;
	}

	private void map(Actor actor, JsonObject obj) {
		if (actor == null || obj == null)
			return;
		JsonWriter.addAttributes(actor, obj, store);
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
