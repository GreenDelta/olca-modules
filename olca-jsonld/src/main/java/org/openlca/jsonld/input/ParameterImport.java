package org.openlca.jsonld.input;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;

import com.google.gson.JsonObject;

class ParameterImport extends BaseImport<Parameter> {

	private ParameterImport(String refId, ImportConfig conf) {
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
		p.setId(id);
		In.mapAtts(json, p);
		p.setCategory(CategoryImport.run(In.getRefId(json, "category"), conf));
		p.setScope(getScope(json));
		p.setInputParameter(In.getBool(json, "inputParameter", true));
		p.setValue(In.getDouble(json, "value", 0));
		p.setFormula(In.getString(json, "formula"));
		p.setExternalSource(In.getString(json, "externalSource"));
		p.setSourceType(In.getString(json, "sourceType"));
		p.setUncertainty(Uncertainties.read(In.getObject(json, "uncertainty")));
		return conf.db.put(p);
	}

	private ParameterScope getScope(JsonObject json) {
		String scope = In.getString(json, "parameterScope");
		if (scope == null)
			return ParameterScope.GLOBAL;
		switch (scope) {
		case "GLOBAL_SCOPE":
			return ParameterScope.GLOBAL;
		case "LCIA_METHOD_SCOPE":
			return ParameterScope.IMPACT_METHOD;
		case "PROCESS_SCOPE":
			return ParameterScope.PROCESS;
		default:
			return ParameterScope.GLOBAL;
		}
	}

}
