package org.openlca.jsonld.output;

import org.openlca.core.model.Parameter;

import com.google.gson.JsonObject;

class ParameterWriter extends Writer<Parameter> {

	ParameterWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(Parameter param) {
		JsonObject obj = super.write(param);
		if (obj == null)
			return null;
		mapAttr(obj, param);
		ParameterReferences.writeReferencedParameters(param, conf);
		return obj;
	}

	static void mapAttr(JsonObject json, Parameter param) {
		addBasicAttributes(param, json);
		Out.put(json, "parameterScope", param.getScope());
		Out.put(json, "inputParameter", param.isInputParameter());
		Out.put(json, "value", param.getValue());
		Out.put(json, "formula", param.getFormula());
		Out.put(json, "externalSource", param.getExternalSource());
		Out.put(json, "sourceType", param.getSourceType());
		Out.put(json, "uncertainty", Uncertainties.map(param.getUncertainty()));
	}

}
