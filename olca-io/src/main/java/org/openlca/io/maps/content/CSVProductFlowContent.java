package org.openlca.io.maps.content;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openlca.io.maps.MapType;

public class CSVProductFlowContent extends AbstractMapContent {

	private final static String JSON_NAME_NAME = "name";

	private String name;

	public CSVProductFlowContent() {
		mapType = MapType.CSV_PRODUCT_FLOW;
	}

	public CSVProductFlowContent(String name) {
		mapType = MapType.CSV_PRODUCT_FLOW;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void fromJson(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		Object object = parser.parse(json);
		JSONObject jsonObject = (JSONObject) object;
		name = (String) jsonObject.get(JSON_NAME_NAME);
	}

	@Override
	public String toJson() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(JSON_NAME_NAME, name);
		return JSONValue.toJSONString(map);
	}

}
