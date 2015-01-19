package org.openlca.jsonld;

import com.google.gson.JsonObject;

final class Context {

	private Context() {
	}

	static void add(JsonObject object) {
		JsonObject context = new JsonObject();
		context.addProperty("@vocab", "http://openlca.org/schema/v1.0/");
		context.addProperty("@base", "http://openlca.org/schema/v1.0/");
		JsonObject vocabType = new JsonObject();
		vocabType.addProperty("@type", "@vocab");
		context.add("modelType", vocabType);
		context.add("flowPropertyType", vocabType);
		context.add("flowType", vocabType);
		context.add("distributionType", vocabType);
		context.add("parameterScope", vocabType);
		context.add("allocationType", vocabType);
		context.add("defaultAllocationMethod", vocabType);
		context.add("processType", vocabType);
		object.add("@context", context);
	}

}
