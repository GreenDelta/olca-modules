package org.openlca.io.maps.content;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openlca.io.maps.MapType;

public class ILCDFlowContent extends AbstractMapContent {

	private static final String JSON_ID_NAME = "id";
	private static final String JSON_VERSION_NAME = "version";

	private String id;
	private String version;

	public ILCDFlowContent() {
		mapType = MapType.ILCD_FLOW;
	}

	public ILCDFlowContent(String id, String version) {
		mapType = MapType.ILCD_FLOW;
		this.id = id;
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public void fromJson(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		Object object = parser.parse(json);
		JSONObject jsonObject = (JSONObject) object;
		id = (String) jsonObject.get(JSON_ID_NAME);
		version = (String) jsonObject.get(JSON_VERSION_NAME);
	}

	@Override
	public String toJson() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(JSON_ID_NAME, id);
		map.put(JSON_VERSION_NAME, version);
		return JSONValue.toJSONString(map);
	}

}
