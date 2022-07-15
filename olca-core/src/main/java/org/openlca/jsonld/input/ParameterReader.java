package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.jsonld.Json;

public record ParameterReader(EntityResolver resolver)
	implements EntityReader<Parameter> {

	public ParameterReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public Parameter read(JsonObject json) {
		var parameter = new Parameter();
		update(parameter, json);
		return parameter;
	}

	@Override
	public void update(Parameter param, JsonObject json) {
		mapFields(param, json, resolver);
	}

	static void mapFields(Parameter param, JsonObject json,
		EntityResolver resolver) {

		Util.mapBase(param, json, resolver);
		param.scope = Json.getEnum(json, "parameterScope", ParameterScope.class);
		param.isInputParameter = Json.getBool(json, "isInputParameter", true);
		param.value = Json.getDouble(json, "value", 0);
		param.formula = Json.getString(json, "formula");
		param.uncertainty = Uncertainties.read(Json.getObject(json, "uncertainty"));
	}
}
