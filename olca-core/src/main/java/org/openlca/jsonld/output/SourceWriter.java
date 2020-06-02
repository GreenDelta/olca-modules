package org.openlca.jsonld.output;

import org.openlca.core.model.Source;

import com.google.gson.JsonObject;

class SourceWriter extends Writer<Source> {

	SourceWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	public JsonObject write(Source source) {
		JsonObject obj = super.write(source);
		if (obj == null)
			return null;
		Out.put(obj, "url", source.url);
		Out.put(obj, "externalFile", source.externalFile);
		Out.put(obj, "textReference", source.textReference);
		Out.put(obj, "year", source.year);
		return obj;
	}

}
