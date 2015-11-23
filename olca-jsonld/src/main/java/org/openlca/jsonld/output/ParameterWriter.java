package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonObject;

class ParameterWriter extends Writer<Parameter> {

	@Override
	JsonObject write(Parameter param, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(param, refFn);
		if (obj == null)
			return null;
		Out.put(obj, "parameterScope", param.getScope());
		Out.put(obj, "inputParameter", param.isInputParameter());
		Out.put(obj, "value", param.getValue());
		Out.put(obj, "formula", param.getFormula());
		Out.put(obj, "externalSource", param.getExternalSource());
		Out.put(obj, "sourceType", param.getSourceType());
		Out.put(obj, "uncertainty", Uncertainties.map(param.getUncertainty()));
		return obj;
	}

}
