package org.openlca.io.maps.content;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openlca.io.maps.MapType;

public class ES1CategoryContent extends AbstractMapContent {

	private static final String JSON_CATEGORY_NAME = "category";
	private static final String JSON_SUB_CATEGORY_NAME = "subCategory";
	private String category;
	private String subCategory;

	public ES1CategoryContent() {
		mapType = MapType.ES1_CATEGORY;
	}

	public ES1CategoryContent(String category, String subCategory) {
		mapType = MapType.ES1_CATEGORY;
		this.category = category;
		this.subCategory = subCategory;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public static String getJsonCategoryName() {
		return JSON_CATEGORY_NAME;
	}

	public static String getJsonSubCategoryName() {
		return JSON_SUB_CATEGORY_NAME;
	}

	@Override
	public void fromJson(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		Object object = parser.parse(json);
		JSONObject jsonObject = (JSONObject) object;
		category = (String) jsonObject.get(JSON_CATEGORY_NAME);
		subCategory = (String) jsonObject.get(JSON_SUB_CATEGORY_NAME);
	}

	@Override
	public String toJson() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(JSON_CATEGORY_NAME, category);
		map.put(JSON_SUB_CATEGORY_NAME, category);
		return JSONValue.toJSONString(map);
	}

}
