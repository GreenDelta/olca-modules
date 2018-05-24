package org.openlca.jsonld.input;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

class ParameterImport extends BaseImport<Parameter> {

	ParameterImport(String refId, ImportConfig conf) {
		super(ModelType.PARAMETER, refId, conf);
	}

	static Parameter run(String refId, ImportConfig conf) {
		return new ParameterImport(refId, conf).run();
	}

	@Override
	Parameter map(JsonObject json, long id) {
		if (json == null)
			return null;
		Parameter p = new Parameter();
		In.mapAtts(json, p, id, conf);
		mapFields(json, p);
		return conf.db.put(p);
	}

	/** Field mappings for processes and LCIA methods. */
	void mapFields(JsonObject json, Parameter p) {
		In.mapAtts(json, p, p.getId());
		p.setScope(Json.getEnum(json, "parameterScope", ParameterScope.class));
		p.setInputParameter(Json.getBool(json, "inputParameter", true));
		p.setValue(Json.getDouble(json, "value", 0));
		p.setFormula(Json.getString(json, "formula"));
		p.setExternalSource(Json.getString(json, "externalSource"));
		p.setSourceType(Json.getString(json, "sourceType"));
		p.setUncertainty(Uncertainties.read(Json.getObject(json, "uncertainty")));
	}

}
