
package org.openlca.proto.io.input;

import java.util.HashMap;
import java.util.Objects;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.proto.ProtoImpactMethod;

public record ImpactMethodReader(EntityResolver resolver)
	implements EntityReader<ImpactMethod, ProtoImpactMethod> {

	@Override
	public ImpactMethod read(ProtoImpactMethod proto) {
		var method = new ImpactMethod();
		update(method, proto);
		return method;
	}

	@Override
	public void update(ImpactMethod method, ProtoImpactMethod proto) {
		Util.mapBase(method, ProtoBox.of(proto), resolver);
		method.code = proto.getCode();
		method.source = Util.getSource(resolver, proto.getSource());
		// first map categories, nw sets will reference them
		mapCategories(proto, method);
		mapNwSets(proto, method);
	}

	private void mapCategories(ProtoImpactMethod proto, ImpactMethod method) {
		method.impactCategories.clear();
		for (int i = 0; i < proto.getImpactCategoriesCount(); i++) {
			var impact = Util.getImpactCategory(
				resolver, proto.getImpactCategories(i));
			if (impact != null) {
				method.impactCategories.add(impact);
			}
		}
	}

	private void mapNwSets(ProtoImpactMethod proto, ImpactMethod method) {

		var nwSets = new HashMap<String, NwSet>();
		for (var nwSet : method.nwSets) {
			nwSets.put(nwSet.refId, nwSet);
		}
		method.nwSets.clear();

		for (int i = 0; i < proto.getNwSetsCount(); i++) {
			var protoNwSet = proto.getNwSets(i);
			var nwSet = nwSets.computeIfAbsent(
				protoNwSet.getId(), rid -> new NwSet());
			method.nwSets.add(nwSet);
			nwSet.refId = protoNwSet.getId();
			nwSet.name = protoNwSet.getName();
			nwSet.description = protoNwSet.getDescription();
			nwSet.weightedScoreUnit = protoNwSet.getWeightedScoreUnit();

			nwSet.factors.clear();

			for (int j = 0; j < protoNwSet.getFactorsCount(); j++) {
				var protoFactor = protoNwSet.getFactors(j);
				var f = new NwFactor();
				var impactId = protoFactor.getImpactCategory().getId();
				f.impactCategory = method.impactCategories.stream()
					.filter(imp -> Objects.equals(imp.refId, impactId))
					.findAny()
					.orElse(null);

				if (protoFactor.hasNormalisationFactor()) {
					f.normalisationFactor = protoFactor.getNormalisationFactor();
				}
				if (protoFactor.hasWeightingFactor()) {
					f.weightingFactor = protoFactor.getWeightingFactor();
				}
				nwSet.factors.add(f);
			}

		}

	}
}
