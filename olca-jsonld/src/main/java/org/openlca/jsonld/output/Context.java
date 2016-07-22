package org.openlca.jsonld.output;

import com.google.gson.JsonObject;

public class Context {

	public static JsonObject write(String version) {
		JsonObject context = new JsonObject();
		Out.put(context, "@vocab", version);
		Out.put(context, "@base", version);
		JsonObject vocabType = new JsonObject();
		Out.put(vocabType, "@type", "@vocab");
		Out.put(context, "modelType", vocabType);
		Out.put(context, "flowPropertyType", vocabType);
		Out.put(context, "flowType", vocabType);
		Out.put(context, "distributionType", vocabType);
		Out.put(context, "parameterScope", vocabType);
		Out.put(context, "allocationType", vocabType);
		Out.put(context, "defaultAllocationMethod", vocabType);
		Out.put(context, "allocationMethod", vocabType);
		Out.put(context, "processType", vocabType);
		Out.put(context, "riskLevel", vocabType);
		return context;
	}

}
