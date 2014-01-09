package org.openlca.io.maps.content;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openlca.io.maps.MapType;

public class ES2CategoryContent extends AbstractMapContent {

	private static final String JSON_COMPARTMENT_NAME = "compartmentId";
	private static final String JSON_SUB_COMPARTMENT_NAME = "subCompartmentId";
	private String compartmentId;
	private String subCompartmentId;

	public ES2CategoryContent() {
		mapType = MapType.ES2_CATEGORY;
	}

	public ES2CategoryContent(String compartmentId, String subCompartmentId) {
		mapType = MapType.ES2_CATEGORY;
		this.compartmentId = compartmentId;
		this.subCompartmentId = subCompartmentId;
	}

	public String getCompartmentId() {
		return compartmentId;
	}

	public void setCompartmentId(String compartmentId) {
		this.compartmentId = compartmentId;
	}

	public String getSubCompartmentId() {
		return subCompartmentId;
	}

	public void setSubCompartmentId(String subCompartmentId) {
		this.subCompartmentId = subCompartmentId;
	}

	@Override
	public void fromJson(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		Object object = parser.parse(json);
		JSONObject jsonObject = (JSONObject) object;
		compartmentId = (String) jsonObject.get(JSON_COMPARTMENT_NAME);
		subCompartmentId = (String) jsonObject.get(JSON_SUB_COMPARTMENT_NAME);
	}

	@Override
	public String toJson() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(JSON_COMPARTMENT_NAME, compartmentId);
		map.put(JSON_SUB_COMPARTMENT_NAME, subCompartmentId);
		return JSONValue.toJSONString(map);
	}

}
