package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ImpactMethodWriter extends Writer<ImpactMethod> {

	@Override
	JsonObject write(ImpactMethod method, Consumer<RootEntity> refHandler) {
		JsonObject obj = super.write(method, refHandler);
		if (obj == null)
			return null;
		JsonArray array = new JsonArray();
		for (ImpactCategory category : method.getImpactCategories()) {
			JsonObject ref = createRef(category, refHandler);
			array.add(ref);
		}
		obj.add("impactCategories", array);
		return obj;
	}

}
