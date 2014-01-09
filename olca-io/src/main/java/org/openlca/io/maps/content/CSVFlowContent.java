package org.openlca.io.maps.content;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openlca.io.maps.MapType;

public class CSVFlowContent extends AbstractMapContent {

	private final static String JSON_NAME_NAME = "name";
	private final static String JSON_UNIT_NAME = "unit";
	private final static String JSON_TYPE_NAME = "type";

	private String name;
	private String unit;
	private String type;

	public CSVFlowContent() {
		mapType = MapType.CSV_FLOW;
	}

	public CSVFlowContent(String name, String unit, String type) {
		mapType = MapType.CSV_FLOW;
		this.name = name;
		this.unit = unit;
		this.type = type;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void fromJson(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		Object object = parser.parse(json);
		JSONObject jsonObject = (JSONObject) object;
		name = (String) jsonObject.get(JSON_NAME_NAME);
		unit = (String) jsonObject.get(JSON_UNIT_NAME);
		type = (String) jsonObject.get(JSON_TYPE_NAME);
	}

	@Override
	public String toJson() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(JSON_NAME_NAME, name);
		map.put(JSON_UNIT_NAME, unit);
		map.put(JSON_TYPE_NAME, type);
		return JSONValue.toJSONString(map);
	}

}
