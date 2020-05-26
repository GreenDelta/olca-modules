package org.openlca.jsonld.input;

import java.util.List;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class NwSetImport extends BaseEmbeddedImport<NwSet, ImpactMethod> {

	private final List<ImpactCategory> impactCategories;

	private NwSetImport(String impactMethodRefId, List<ImpactCategory> impactCategories, ImportConfig conf) {
		super(ModelType.IMPACT_METHOD, impactMethodRefId, conf);
		this.impactCategories = impactCategories;
	}

	static NwSet run(String impactMethodRefId, List<ImpactCategory> impactCategories, JsonObject json, ImportConfig conf) {
		return new NwSetImport(impactMethodRefId, impactCategories, conf).run(json);
	}

	@Override
	NwSet map(JsonObject json, long id) {
		if (json == null)
			return null;
		NwSet set = new NwSet();
		In.mapAtts(json, set, id);
		set.weightedScoreUnit = Json.getString(json, "weightedScoreUnit");
		JsonArray factors = Json.getArray(json, "factors");
		if (factors == null)
			return set;
		for (JsonElement f : factors) {
			if (!f.isJsonObject())
				continue;
			NwFactor factor = mapFactor(f.getAsJsonObject());
			set.factors.add(factor);
		}
		return set;
	}

	private NwFactor mapFactor(JsonObject json) {
		NwFactor f = new NwFactor();
		String categoryId = Json.getRefId(json, "impactCategory");
		f.impactCategory = getImpactCategory(categoryId);
		f.normalisationFactor = Json.getDouble(json, "normalisationFactor")
				.orElse(null);
		f.weightingFactor = Json.getDouble(json, "weightingFactor")
				.orElse(null);
		return f;
	}

	private ImpactCategory getImpactCategory(String refId) {
		for (ImpactCategory category : impactCategories)
			if (category.refId.equals(refId))
				return category;
		return null;
	}

	@Override
	NwSet getPersisted(ImpactMethod impactMethod, JsonObject json) {
		String refId = Json.getString(json, "@id");
		if (refId == null)
			return null;
		for (NwSet nwSet : impactMethod.nwSets)
			if (refId.equals(nwSet.refId))
				return nwSet;
		return null;
	}

}
