package org.openlca.core.matrix;

import java.util.HashMap;
import java.util.HashSet;

import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.model.descriptors.ImpactDescriptor;

import gnu.trove.map.hash.TLongObjectHashMap;

/// Collects and applies default characterization factors (CFs) when a
/// regionalized characterization matrix is built. A default CF is not bound
/// to a specific location, in contrast to a regionalized CF. When there is
/// no regionalized CF available for a specific impact category and
/// flow-location pair, the default CF is applied for that combination.
class ImpactDefaultsMap {

	private final ImpactIndex impactIndex;
	private final EnviIndex enviIndex;
	private final TLongObjectHashMap<CalcImpactFactor>[] defaultFactors;
	private final HashMap<Long, HashSet<LongPair>> added =new HashMap<>();

	@SuppressWarnings("unchecked")
	ImpactDefaultsMap(ImpactIndex impactIndex, EnviIndex enviIndex) {
		this.impactIndex = impactIndex;
		this.enviIndex = enviIndex;
		defaultFactors = new TLongObjectHashMap[impactIndex.size()];
		for (int i = 0; i < impactIndex.size(); i++) {
			defaultFactors[i] = new TLongObjectHashMap<>();
		}
	}

	void put(int impactRow, CalcImpactFactor factor) {
		defaultFactors[impactRow].put(factor.flowId, factor);
	}

	void markAdded(long impactId, LongPair regioFlow) {
		added.computeIfAbsent(impactId, $ -> new HashSet<>())
				.add(regioFlow);
	}

	void apply(DefaultConsumer fn) {
		for (int row = 0; row < impactIndex.size(); row++) {
			var impact = impactIndex.at(row);
			var m = defaultFactors[row];
			if (m.isEmpty())
				continue;

			for (int col = 0; col < enviIndex.size(); col++) {
				var enviFlow = enviIndex.at(col);
				long flowId = enviFlow.flow().id;
				long locId = enviFlow.location() != null
						? enviFlow.location().id
						: 0L;
				if (wasAdded(impact, flowId, locId))
					continue;

				var factor = m.get(flowId);
				if (factor == null)
					continue;
				fn.apply(row, col, factor);
			}
		}
	}

	private boolean wasAdded(ImpactDescriptor impact, long flowId, long locId) {
		var ids = added.get(impact.id);
		if (ids == null || ids.isEmpty())
			return false;
		return ids.contains(LongPair.of(flowId, locId));
	}

	@FunctionalInterface
	interface DefaultConsumer {
		void apply(int row, int col, CalcImpactFactor factor);
	}
}
