package org.openlca.jsonld;

import java.lang.reflect.Type;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;

class ActorWriter implements Writer<Actor> {

	@Override
	public void write(Actor actor, EntityStore store) {
		if(actor == null || store == null)
			return;
		if(store.contains(ModelType.ACTOR, actor.getRefId()))
			return;
		JsonObject obj = serialize(actor, null, null);
		store.add(ModelType.ACTOR, obj);
	}

	@Override
	public JsonObject serialize(Actor actor, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		map(actor, obj);
		return obj;
	}

	static void map(Actor actor, JsonObject obj) {
		if(actor == null || obj == null)
			return;
		JsonWriter.addAttributes(actor, obj);
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
