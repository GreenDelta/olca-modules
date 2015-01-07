package org.openlca.jsonld;

import com.google.gson.JsonObject;

final class Context {

	private Context() {
	}

	static void add(JsonObject object) {
		String url = "http://openlca.org/schema/v1.0#";
		JsonObject context = new JsonObject();
		context.addProperty("@vocab", url);
		JsonObject vocabType = new JsonObject();
		vocabType.addProperty("@type", "@vocab");
		context.add("modelType", vocabType);
		context.add("flowPropertyType", vocabType);
		context.add("flowType", vocabType);
		context.add("distributionType", vocabType);
		context.add("parameterScope", vocabType);
		context.add("allocationType", vocabType);
		context.add("defaultAllocationMethod", vocabType);
		context.add("processTyp", vocabType);
		object.add("@context", context);
	}

}
