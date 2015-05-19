package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ActorImport {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	private String refId;
	private ImportConfig conf;
	
	private ActorImport(String refId, ImportConfig conf) {
		this.refId = refId;
		this.conf = conf;
	}
	
	static Actor run(String refId, ImportConfig conf) {
		return new ActorImport(refId, conf).run();
	}
	
	private Actor run() {
		if (refId == null || conf == null)
			return null;
		try {
			Actor a = conf.db.getActor(refId);
			if (a != null)
				return a;
			JsonObject json = conf.store.get(ModelType.ACTOR, refId);
			return map(json);
		} catch (Exception e) {
			log.error("failed to import actor " + refId, e);
			return null;
		}
	}
	
	private Actor map(JsonObject json) {
		if (json == null)
			return null;
		Actor a = new Actor();
		In.mapAtts(json, a);
		String catId = In.getRefId(json, "category");
		a.setCategory(CategoryImport.run(catId, conf));
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

