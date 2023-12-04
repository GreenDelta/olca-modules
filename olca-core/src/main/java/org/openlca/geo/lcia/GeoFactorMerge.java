package org.openlca.geo.lcia;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.RootEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeoFactorMerge {

	private final ImpactCategory impact;
	private final boolean keepExisting;

	private GeoFactorMerge(ImpactCategory impact, boolean keepExisting) {
		this.impact = impact;
		this.keepExisting = keepExisting;
	}

	public static GeoFactorMerge keepExisting(ImpactCategory impact) {
		return new GeoFactorMerge(impact, true);
	}

	public static GeoFactorMerge replaceExisting(ImpactCategory impact) {
		return new GeoFactorMerge(impact, false);
	}

	public void doIt(List<ImpactFactor> newFactors) {
		if (impact == null || newFactors == null || newFactors.isEmpty())
			return;
		if (impact.impactFactors.isEmpty()) {
			impact.impactFactors.addAll(newFactors);
			return;
		}
		if (keepExisting) {
			var idx = index(impact.impactFactors);
			var temp = new ArrayList<>(newFactors);
			drop(temp, idx);
			impact.impactFactors.addAll(temp);
		} else {
			var idx = index(newFactors);
			drop(impact.impactFactors, idx);
			impact.impactFactors.addAll(newFactors);
		}
	}

	/**
	 * Creates a flow-locations index for the used flow-location
	 * pairs in the given factors.
	 */
	private Map<Long, Set<Long>> index(List<ImpactFactor> factors) {
		var idx = new HashMap<Long, Set<Long>>();
		for (var f : factors) {
			idx.computeIfAbsent(id(f.flow), $ -> new HashSet<>())
					.add(id(f.location));
		}
		return idx;
	}

	/**
	 * Removes all flow-location combinations from the given factor
	 * list, that are contained in the given index.
	 */
	private void drop(List<ImpactFactor> factors, Map<Long, Set<Long>> idx) {
		var removals = new ArrayList<ImpactFactor>();
		for (var f : factors) {
			var locations = idx.get(id(f.flow));
			if (locations != null && locations.contains(id(f.location))) {
				removals.add(f);
			}
		}
		factors.removeAll(removals);
	}

	private long id(RootEntity e) {
		return e != null ? e.id : 0L;
	}

}
