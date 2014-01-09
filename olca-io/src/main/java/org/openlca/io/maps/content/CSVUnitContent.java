package org.openlca.io.maps.content;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openlca.io.maps.MapType;

public class CSVUnitContent extends AbstractMapContent {

	private static final String JSON_UNIT_NAME = "unit";
	private String unit;

	public CSVUnitContent() {
		mapType = MapType.CSV_UNIT;
	}

	public CSVUnitContent(String unit) {
		this.unit = unit;
		mapType = MapType.CSV_UNIT;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@Override
	public void fromJson(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		Object object = parser.parse(json);
		JSONObject jsonObject = (JSONObject) object;
		unit = (String) jsonObject.get(JSON_UNIT_NAME);
	}

	@Override
	public String toJson() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(JSON_UNIT_NAME, unit);
		return JSONValue.toJSONString(map);
	}

}
