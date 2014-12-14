package org.openlca.jsonld;

import java.lang.reflect.Type;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

class SourceWriter implements Writer<Source> {

	private EntityStore store;

	public SourceWriter() {
	}

	public SourceWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public void skipContext() {
	}

	@Override
	public void write(Source source) {
		if (source == null || store == null)
			return;
		if (store.contains(ModelType.SOURCE, source.getRefId()))
			return;
		JsonObject obj = serialize(source, null, null);
		store.add(ModelType.SOURCE, source.getRefId(), obj);
	}

	@Override
	public JsonObject serialize(Source source, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		JsonWriter.addAttributes(source, obj, store);
		obj.addProperty("doi", source.getDoi());
		obj.addProperty("externalFile", source.getExternalFile());
		obj.addProperty("textReference", source.getTextReference());
		obj.addProperty("year", source.getYear());
		return obj;
	}

}
