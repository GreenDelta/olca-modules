package org.openlca.jsonld.input;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.SocialIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

class SocialIndicatorImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private String refId;
	private ImportConfig conf;

	private SocialIndicatorImport(String refId, ImportConfig conf) {
		this.refId = refId;
		this.conf = conf;
	}

	static SocialIndicator run(String refId, ImportConfig conf) {
		return new SocialIndicatorImport(refId, conf).run();
	}

	private SocialIndicator run() {
		if (refId == null || conf == null)
			return null;
		try {
			SocialIndicator i = conf.db.getSocialIndicator(refId);
			if (i != null)
				return i;
			JsonObject json = conf.store.get(ModelType.SOCIAL_INDICATOR, refId);
			return map(json);
		} catch (Exception e) {
			log.error("failed to import social indicator " + refId, e);
			return null;
		}
	}

	private SocialIndicator map(JsonObject json) {
		if (json == null)
			return null;
		SocialIndicator i = new SocialIndicator();
		In.mapAtts(json, i);
		String catId = In.getRefId(json, "category");
		i.setCategory(CategoryImport.run(catId, conf));
		i.activityVariable = In.getString(json, "activityVariable");
		i.evaluationScheme = In.getString(json, "evaluationScheme");
		i.unitOfMeasurement = In.getString(json, "unitOfMeasurement");
		// import the quantity before setting the unit to assure that the
		// unit is already in the database
		String quanId = In.getRefId(json, "activityQuantity");
		i.activityQuantity = FlowPropertyImport.run(quanId, conf);
		String unitId = In.getRefId(json, "activityUnit");
		i.activityUnit = conf.db.getUnit(unitId);
		return conf.db.put(i);
	}

}
