package org.openlca.io.maps.content;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openlca.io.maps.MapType;
import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.SubCompartment;

public class CSVElementaryFlowContent extends AbstractMapContent {

	private final static String JSON_NAME_NAME = "name";
	private final static String JSON_UNIT_NAME = "unit";
	private final static String JSON_TYPE_NAME = "type";
	private final static String JSON_SUB_COMPARTMENT_NAME = "subCompartment";

	private String name;
	private String unit;
	private ElementaryFlowType type;
	private SubCompartment subCompartment;

	public CSVElementaryFlowContent() {
		mapType = MapType.CSV_ELEMENTARY_FLOW;
	}

	public CSVElementaryFlowContent(String name, String unit,
			ElementaryFlowType type, SubCompartment subCompartment) {
		mapType = MapType.CSV_ELEMENTARY_FLOW;
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

	public ElementaryFlowType getType() {
		return type;
	}

	public void setType(ElementaryFlowType type) {
		this.type = type;
	}

	public SubCompartment getSubCompartment() {
		return subCompartment;
	}

	public void setSubCompartment(SubCompartment subCompartment) {
		this.subCompartment = subCompartment;
	}

	@Override
	public void fromJson(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		Object object = parser.parse(json);
		JSONObject jsonObject = (JSONObject) object;
		name = (String) jsonObject.get(JSON_NAME_NAME);
		unit = (String) jsonObject.get(JSON_UNIT_NAME);
		type = ElementaryFlowType.forValue((String) jsonObject
				.get(JSON_TYPE_NAME));
		subCompartment = SubCompartment.forValue((String) jsonObject
				.get(JSON_SUB_COMPARTMENT_NAME));
	}

	@Override
	public String toJson() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(JSON_NAME_NAME, name);
		map.put(JSON_UNIT_NAME, unit);
		map.put(JSON_TYPE_NAME, type.getValue());
		map.put(JSON_SUB_COMPARTMENT_NAME, subCompartment.getValue());
		return JSONValue.toJSONString(map);
	}

}
