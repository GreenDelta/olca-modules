package org.openlca.io.maps.content;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openlca.io.maps.MapType;

public class ES1UnitContent extends AbstractMapContent {

	private static final String JSON_ID_NAME = "id";
	private String id;

	public ES1UnitContent() {
		mapType = MapType.ES1_UNIT;
	}

	public ES1UnitContent(String id) {
		mapType = MapType.ES1_UNIT;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void fromJson(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		Object object = parser.parse(json);
		JSONObject jsonObject = (JSONObject) object;
		id = (String) jsonObject.get(JSON_ID_NAME);

	}

	@Override
	public String toJson() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(JSON_ID_NAME, id);
		return JSONValue.toJSONString(map);
	}

}
