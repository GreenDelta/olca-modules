package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.CostCategory;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonObject;

class CostCategoryWriter extends Writer<CostCategory> {

	@Override
	JsonObject write(CostCategory cc, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(cc, refFn);
		if (obj == null)
			return null;
		return obj;
	}
}
