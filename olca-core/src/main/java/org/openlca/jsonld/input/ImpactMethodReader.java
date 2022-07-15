package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

public record ImpactMethodReader(EntityResolver resolver)
	implements EntityReader<ImpactMethod> {

	public ImpactMethodReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public ImpactMethod read(JsonObject json) {
		var method = new ImpactMethod();
		update(method, json);
		return method;
	}

	@Override
	public void update(ImpactMethod method, JsonObject json) {
		Util.mapBase(method, json, resolver);
		method.code = Json.getString(json, "code");
		var sourceId = Json.getString(json, "source");
		method.source = resolver.get(Source.class, sourceId);
		// first map categories, nw sets will reference them
		mapCategories(json, method);
		mapNwSets(json, method);
	}

	private void mapCategories(JsonObject json, ImpactMethod method) {
		method.impactCategories.clear();
		var array = Json.getArray(json, "impactCategories");
		if (array == null || array.size() == 0)
			return;
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var catId = Json.getString(e.getAsJsonObject(), "@id");
			var impact = resolver.get(ImpactCategory.class, catId);
			if (impact != null) {
				method.impactCategories.add(impact);
			}
		}
	}

	private void mapNwSets(JsonObject json, ImpactMethod method) {

		var nwSets = new HashMap<String, NwSet>();
		for (var nwSet : method.nwSets) {
			nwSets.put(nwSet.refId, nwSet);
		}
		method.nwSets.clear();
		var array = Json.getArray(json, "nwSets");
		if (array == null)
			return;

		for (JsonElement e : array) {
			if (!e.isJsonObject())
				continue;
			var nwObj = e.getAsJsonObject();
			var refId = Json.getString(nwObj, "@id");
			if (Strings.nullOrEmpty(refId))
				continue;
			var nwSet = nwSets.computeIfAbsent(refId, rid -> new NwSet());
			method.nwSets.add(nwSet);
			Util.mapBase(nwSet, nwObj, resolver);
			nwSet.weightedScoreUnit = Json.getString(
				json, "weightedScoreUnit");
			nwSet.factors.clear();
			Json.stream(Json.getArray(nwObj, "factors"))
				.filter(JsonElement::isJsonObject)
				.map(f -> nwFactor(f.getAsJsonObject(), method))
				.forEach(nwSet.factors::add);
		}
	}

	private NwFactor nwFactor(JsonObject json, ImpactMethod method) {
		var f = new NwFactor();
		var impactID = Json.getRefId(json, "impactCategory");
		f.impactCategory = method.impactCategories.stream()
			.filter(i -> Objects.equals(i.refId, impactID))
			.findAny()
			.orElse(null);
		var norm = Json.getDouble(json, "normalisationFactor");
		f.normalisationFactor = norm.isPresent()
			? norm.getAsDouble()
			: null;
		var weight = Json.getDouble(json, "weightingFactor");
		f.weightingFactor = weight.isPresent()
			? weight.getAsDouble()
			: null;
		return f;
	}
}
