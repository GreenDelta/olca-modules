package org.openlca.jsonld.output;

import org.openlca.jsonld.Schema;

import com.google.gson.JsonObject;

public class Context {

	public static JsonObject write() {
		JsonObject context = new JsonObject();
		Out.put(context, "@vocab", Schema.URI);
		Out.put(context, "@base", Schema.URI);
		JsonObject vocabType = new JsonObject();
		Out.put(vocabType, "@type", "@vocab");
		Out.put(context, "modelType", vocabType);
		Out.put(context, "flowPropertyType", vocabType);
		Out.put(context, "flowType", vocabType);
		Out.put(context, "distributionType", vocabType);
		Out.put(context, "parameterScope", vocabType);
		Out.put(context, "allocationType", vocabType);
		Out.put(context, "defaultAllocationMethod", vocabType);
		Out.put(context, "processType", vocabType);
		Out.put(context, "riskLevel", vocabType);
		return context;
	}

}
