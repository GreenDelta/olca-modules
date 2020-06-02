package org.openlca.jsonld.output;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;

class MetaInfoWriter {
	
	private final ExportConfig conf;
	
	MetaInfoWriter(ExportConfig conf) {
		this.conf = conf;
	}

	public JsonObject write() {
		JsonObject obj = new JsonObject();
		String client = conf.clientInfo;
		if (Strings.isNullOrEmpty(client)) {
			client = "Unknown";
		}
		obj.addProperty("client", client);
		return obj;
	}

}
