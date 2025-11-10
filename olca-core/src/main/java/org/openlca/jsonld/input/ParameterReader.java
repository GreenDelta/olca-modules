package org.openlca.jsonld.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.commons.Strings;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

	public static List<ParameterRedef> readRedefs(
			JsonArray array, EntityResolver resolver) {
		if (array == null || array.isEmpty())
			return Collections.emptyList();
		var redefs = new ArrayList<ParameterRedef>();
		for (var elem : array) {
			if (!elem.isJsonObject())
				continue;
			var object = elem.getAsJsonObject();
			var p = new ParameterRedef();
			p.name = Json.getString(object, "name");
			p.description = Json.getString(object, "description");
			p.value = Json.getDouble(object, "value", 0);
			p.uncertainty = Uncertainties.read(Json
					.getObject(object, "uncertainty"));
			p.isProtected = Json.getBool(object, "isProtected", false);
			var context = getContext(Json.getObject(object, "context"), resolver);
			if (context != null) {
				p.contextType = context.type;
				p.contextId = context.id;
			}
			redefs.add(p);
		}
		return redefs;
	}

	private static Descriptor getContext(
			JsonObject context, EntityResolver resolver) {
		if (context == null)
			return null;
		var type = Json.getString(context, "@type");
		var refId = Json.getString(context, "@id");
		if (Strings.isBlank(refId))
			return null;
		return "ImpactCategory".equals(type)
				? resolver.getDescriptor(ImpactCategory.class, refId)
				: resolver.getDescriptor(Process.class, refId);
	}
}
