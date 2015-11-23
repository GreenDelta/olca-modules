package org.openlca.jsonld;

import com.google.gson.JsonElement;

public class Schema {

	public static final String URI = "http://openlca.org/schema/v1.0/";
	
	public static String parseUri(JsonElement element) {
		if (!element.isJsonObject())
			return null;
		JsonElement vocab = element.getAsJsonObject().get("@vocab");
		if (vocab == null)
			return null;
		return vocab.getAsString();
	}
	
}
