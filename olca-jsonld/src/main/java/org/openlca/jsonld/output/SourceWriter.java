package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;

import com.google.gson.JsonObject;

class SourceWriter extends Writer<Source> {

	@Override
	public JsonObject write(Source source, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(source, refFn);
		if (obj == null)
			return null;
		obj.addProperty("doi", source.getDoi());
		obj.addProperty("externalFile", source.getExternalFile());
		obj.addProperty("textReference", source.getTextReference());
		obj.addProperty("year", source.getYear());
		return obj;
	}

}
