package org.openlca.jsonld;

import com.google.gson.JsonElement;

public class Schema {

	public static final String URI = "http://openlca.org/schema/v1.1/";
	public static final String CONTEXT_URI = "http://greendelta.github.io/olca-schema/context.jsonld";
	private static final String[] SUPPORTED = { URI };

	public static boolean isSupportedSchema(String version) {
		for (String supported : SUPPORTED)
			if (supported.equals(version))
				return true;
		return false;
	}

	public static String parseUri(JsonElement context) {
		if (context == null)
			return null;
		if (!context.isJsonObject())
			return null;
		JsonElement vocab = context.getAsJsonObject().get("@vocab");
		if (vocab == null)
			return null;
		return vocab.getAsString();
	}

	public static class UnsupportedSchemaException extends Error {

		private static final long serialVersionUID = 1916423824713840333L;

		public UnsupportedSchemaException(String unsupportedSchema) {
			super("Schema " + unsupportedSchema + " unsupported - current schema is " + URI);
		}
	}

}
