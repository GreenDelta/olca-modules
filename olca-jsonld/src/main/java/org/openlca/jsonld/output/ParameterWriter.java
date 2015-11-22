package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Uncertainty;
import org.openlca.jsonld.Enums;

import com.google.gson.JsonObject;

class ParameterWriter extends Writer<Parameter> {

	@Override
	JsonObject write(Parameter parameter, Consumer<RootEntity> refHandler) {
		JsonObject obj = super.write(parameter, refHandler);
		obj.addProperty("parameterScope",
				Enums.getLabel(parameter.getScope(), ParameterScope.class));
		obj.addProperty("inputParameter", parameter.isInputParameter());
		obj.addProperty("value", parameter.getValue());
		obj.addProperty("formula", parameter.getFormula());
		obj.addProperty("externalSource", parameter.getExternalSource());
		obj.addProperty("sourceType", parameter.getSourceType());
		mapUncertainty(parameter, obj);
		return obj;
	}

	private void mapUncertainty(Parameter parameter, JsonObject obj) {
		Uncertainty uncertainty = parameter.getUncertainty();
		if (uncertainty == null)
			return;
		JsonObject uncertaintyObj = new JsonObject();
		Uncertainties.map(uncertainty, uncertaintyObj);
		obj.add("uncertainty", uncertaintyObj);
	}

}
