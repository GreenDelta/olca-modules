package org.openlca.jsonld.input;

import org.openlca.core.model.CostCategory;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;

class CostCategoryImport extends BaseImport<CostCategory> {

	private CostCategoryImport(String refId, ImportConfig conf) {
		super(ModelType.COST_CATEGORY, refId, conf);
	}

	static CostCategory run(String refId, ImportConfig conf) {
		return new CostCategoryImport(refId, conf).run();
	}

	@Override
	CostCategory map(JsonObject json, long id) {
		if (json == null)
			return null;
		CostCategory cc = new CostCategory();
		In.mapAtts(json, cc, id, conf);
		return conf.db.put(cc);
	}

}
