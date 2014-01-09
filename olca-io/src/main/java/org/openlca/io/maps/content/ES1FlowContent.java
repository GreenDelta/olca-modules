package org.openlca.io.maps.content;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openlca.io.maps.MapType;

public class ES1FlowContent extends AbstractMapContent {

	private final static String JSON_NAME_NAME = "name";
	private final static String JSON_UNIT_NAME = "unit";
	private final static String JSON_CATEGORY_NAME = "category";
	private final static String JSON_SUB_CATEGORY_NAME = "subCategory";
	private final static String JSON_CAS_NUMBER_NAME = "casNumber";

	private String name;
	private String unit;
	private String category;
	private String subCategory;
	private String casNumber;

	public ES1FlowContent() {
		mapType = MapType.ES1_FLOW;
	}

	public ES1FlowContent(String name, String unit, String category,
			String subCategory, String casNumber) {
		mapType = MapType.ES1_FLOW;
		this.name = name;
		this.unit = unit;
		this.category = category;
		this.subCategory = subCategory;
		this.casNumber = casNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
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

	public String getCasNumber() {
		return casNumber;
	}

	public void setCasNumber(String casNumber) {
		this.casNumber = casNumber;
	}

	@Override
	public void fromJson(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		Object object = parser.parse(json);
		JSONObject jsonObject = (JSONObject) object;
		name = (String) jsonObject.get(JSON_NAME_NAME);
		unit = (String) jsonObject.get(JSON_UNIT_NAME);
		category = (String) jsonObject.get(JSON_CATEGORY_NAME);
		subCategory = (String) jsonObject.get(JSON_SUB_CATEGORY_NAME);
		casNumber = (String) jsonObject.get(JSON_CAS_NUMBER_NAME);
	}

	@Override
	public String toJson() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(JSON_NAME_NAME, name);
		map.put(JSON_UNIT_NAME, unit);
		map.put(JSON_CATEGORY_NAME, category);
		map.put(JSON_SUB_CATEGORY_NAME, subCategory);
		map.put(JSON_CAS_NUMBER_NAME, casNumber);
		return JSONValue.toJSONString(map);
	}

}
