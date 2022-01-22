package org.openlca.jsonld.input;

import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The template for importing embedded things like exchanges in processes or
 * units in unit groups. The main problem that is solved here is to decide
 * whether such an embedded thing needs to be created or updated. In case of an
 * update, we try to keep the IDs stable to not break things in the database.
 *
 * @param <T>
 *            the type of the embedded thing, e.g. an Exchange
 * @param <P>
 *            the type where it is embedded, e.g. a Process
 */
abstract class BaseEmbeddedImport<T extends AbstractEntity, P extends RootEntity> {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final P parent;
	JsonImport conf;

	BaseEmbeddedImport(ModelType parentType, String parentRefId, JsonImport conf) {
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
			long id = model == null ? 0L : model.id;
			return map(json, id);
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

	/**
	 * Create the embedded object and assign the given ID.
	 */
	abstract T map(JsonObject json, long id);

	/**
	 * Get the embedded object from the given model using the information in the
	 * given JSON data.
	 */
	abstract T getPersisted(P parent, JsonObject json);

}
