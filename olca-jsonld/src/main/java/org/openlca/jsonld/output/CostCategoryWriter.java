package org.openlca.jsonld.output;

import org.openlca.core.model.CostCategory;

import com.google.gson.JsonObject;

class CostCategoryWriter extends Writer<CostCategory> {

	CostCategoryWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(CostCategory cc) {
		return super.write(cc);
	}
}
