package org.openlca.jsonld.input;

import org.openlca.core.model.ModelType;
import org.openlca.commons.Strings;

public class CategoryImport {

	public static final String FILE_NAME = "categories.json";
	private final JsonImport imp;
	
	public CategoryImport(JsonImport imp) {
		this.imp = imp;
	}
	
	public void importAll() {
		var categories = imp.reader.getJson(FILE_NAME);
		if (categories == null || !categories.isJsonArray())
			return;
		for (var category : categories.getAsJsonArray()) {
			if (!category.isJsonPrimitive())
				continue;
			var value = category.getAsString();
			if (Strings.isBlank(value))
				continue;
			if (value.startsWith("/")) {
				value = value.substring(1);
			}
			if (value.endsWith("/")) {
				value = value.substring(0, value.length() - 1);
			}
			if (!value.contains("/")) 
				continue;
			if (value.contains("//"))
				continue;
			var type = safeModelType(value);
			if (type == null)
				continue;
			var path = value.substring(value.indexOf("/") + 1);
			imp.getCategory(type, path);
		}
	}

	private ModelType safeModelType(String value) {
		var type = value.substring(0, value.indexOf("/"));
		for (var mType : ModelType.values())
			if (mType.name().equals(type))
				return mType;
		return null;
	}
	
}
