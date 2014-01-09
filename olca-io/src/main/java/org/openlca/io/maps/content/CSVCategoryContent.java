package org.openlca.io.maps.content;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openlca.io.maps.MapType;

public class CSVCategoryContent extends AbstractMapContent {

	private final static String JSON_CATEGORY_NAME = "category";
	private String category;

	public CSVCategoryContent() {
		mapType = MapType.CSV_CATEGORY;
	}

	public CSVCategoryContent(String category) {
		mapType = MapType.CSV_CATEGORY;
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public void fromJson(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		Object object = parser.parse(json);
		JSONObject jsonObject = (JSONObject) object;
		category = (String) jsonObject.get(JSON_CATEGORY_NAME);
	}

	@Override
	public String toJson() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(JSON_CATEGORY_NAME, category);
		return JSONValue.toJSONString(map);
	}

}
