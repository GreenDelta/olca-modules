package org.openlca.jsonld.input;

import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

abstract class BaseEmbeddedImport<T extends AbstractEntity, P extends RootEntity> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private P parent;
	ImportConfig conf;

	BaseEmbeddedImport(ModelType parentType, String parentRefId, ImportConfig conf) {
		this.conf = conf;
		this.parent = conf.db.get(parentType, parentRefId);
	}

	final T run(JsonObject json) {
		if (conf == null)
			return null;
		try {
			T model = null;
			if (parent != null) {
				model = getPersisted(parent, json);
				if (!doImport(model, json))
					return model;
			}
			return map(json, model);
		} catch (Exception e) {
			log.error("failed to import embedded object", e);
			return null;
		}
	}

	private boolean doImport(T model, JsonObject json) {
		if (model == null)
			return true;
		if (json == null || conf.updateMode == UpdateMode.NEVER)
			return false;
		if (conf.updateMode == UpdateMode.ALWAYS)
			return true;
		if (!(model instanceof RootEntity))
			return true;
		return In.isNewer(json, (RootEntity) model);
	}

	T map(JsonObject json, T model) {
		if (model == null)
			return map(json, 0l);
		return map(json, model.id);
	}

	abstract T map(JsonObject json, long id);

	abstract T getPersisted(P parent, JsonObject json);

}
