package org.openlca.io.maps.content;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openlca.io.maps.MapType;

public class ILCDCategoryContent extends AbstractMapContent {

	private static final String JSON_LEVEL_NAME = "level ";
	private List<String> categories = new ArrayList<>();

	public ILCDCategoryContent() {
		mapType = MapType.ILCD_CATEGORY;
	}

	public ILCDCategoryContent(List<String> categories) {
		mapType = MapType.ILCD_CATEGORY;
		this.categories = categories;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	@Override
	public void fromJson(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		Object object = parser.parse(json);
		@SuppressWarnings("unchecked")
		Map<String, String> map = (JSONObject) object;
		categories.clear();
		for (String s : map.values())
			categories.add(s);
	}

	@Override
	public String toJson() {
		Map<String, String> map = new LinkedHashMap<>();
		for (int i = 0; i < categories.size(); i++)
			map.put(JSON_LEVEL_NAME + i, categories.get(i));
		return JSONValue.toJSONString(map);
	}
}
