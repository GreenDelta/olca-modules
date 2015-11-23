package org.openlca.jsonld.input;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.SocialIndicator;

import com.google.gson.JsonObject;

class SocialIndicatorImport extends BaseImport<SocialIndicator> {

	private SocialIndicatorImport(String refId, ImportConfig conf) {
		super(ModelType.SOCIAL_INDICATOR, refId, conf);
	}

	static SocialIndicator run(String refId, ImportConfig conf) {
		return new SocialIndicatorImport(refId, conf).run();
	}

	@Override
	SocialIndicator map(JsonObject json, long id) {
		if (json == null)
			return null;
		SocialIndicator i = new SocialIndicator();
		In.mapAtts(json, i, id, conf);
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
