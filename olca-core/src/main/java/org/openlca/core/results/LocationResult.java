package org.openlca.core.results;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

import com.google.common.util.concurrent.AtomicDouble;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Calculates the contributions of the locations to a result.
 */
public class LocationResult {

	private final IDatabase db;
	private final LcaResult result;
	private final TLongObjectHashMap<Location> cache = new TLongObjectHashMap<>();

	public LocationResult(LcaResult result, IDatabase db) {
		this.result = result;
		this.db = db;
	}

	/**
	 * Calculates location contributions to the given inventory flow.
	 */
	public List<Contribution<Location>> getContributions(FlowDescriptor flow) {
		if (flow == null || result == null || !result.hasEnviFlows())
			return Collections.emptyList();

		var flowIndex = result.enviIndex();
		var cons = new HashMap<Location, Double>();
		double total;
		if (!flowIndex.isRegionalized()) {
			// non-regionalized calculation;
			// the flow is mapped to a single row
			// we take the locations from the processes
			// in the columns
			int idx = flowIndex.of(flow);
			EnviFlow iFlow = flowIndex.at(idx);
			if (iFlow == null)
				return Collections.emptyList();
			total = result.getTotalFlowValueOf(iFlow);
			result.techIndex().each((i, product) -> {
				Location loc = getLocation(product);
				double v = result.getDirectFlowOf(iFlow, product);
				cons.compute(loc, (_loc, oldVal) -> oldVal == null ? v : oldVal + v);
			});

		} else {
			// regionalized calculation;
			// the flow is mapped to multiple rows where
			// each row specifies the location
			AtomicDouble t = new AtomicDouble();
			flowIndex.each((i, iFlow) -> {
				if (!Objects.equals(flow, iFlow.flow()))
					return;
				Location loc = iFlow.location() == null
						? null
						: getLocation(iFlow.location().id);
				double v = result.getTotalFlowValueOf(iFlow);
				t.addAndGet(v);
				cons.compute(loc, (_loc, oldVal) -> oldVal == null ? v : oldVal + v);
			});
			total = t.get();
		}

		return asContributions(cons, total);
	}

	/**
	 * Calculates location contributions to the given LCIA category.
	 */
	public List<Contribution<Location>> getContributions(
			ImpactDescriptor impact) {
		if (impact == null || result == null || !result.hasImpacts())
			return Collections.emptyList();

		HashMap<Location, Double> cons = new HashMap<>();
		double total = result.getTotalImpactValueOf(impact);

		if (!result.enviIndex().isRegionalized()) {
			// non-regionalized calculation;
			// we take the locations from the processes
			// in the columns and the results from the
			// corresponding process contributions
			result.techIndex().each((i, techFlow) -> {
				Location loc = getLocation(techFlow);
				double v = result.getDirectImpactOf(impact, techFlow);
				cons.compute(loc,
						(_loc, oldVal) -> oldVal == null ? v : oldVal + v);
			});

		} else {
			// regionalized calculation;
			// we take the location from the index flows
			// and the values from the direct contributions
			// of these flows to the LCIA category result
			result.enviIndex().each((i, iFlow) -> {
				Location loc = iFlow.location() == null
						? null
						: getLocation(iFlow.location().id);
				double v = result.getFlowImpactOf(impact, iFlow);
				cons.compute(loc,
						(_loc, oldVal) -> oldVal == null ? v : oldVal + v);
			});

		}

		return asContributions(cons, total);
	}

	/**
	 * Calculates location contributions to the total added value.
	 */
	public List<Contribution<Location>> getAddedValueContributions() {
		if (result == null)
			return Collections.emptyList();
		HashMap<Location, Double> cons = new HashMap<>();
		result.techIndex().each((i, product) -> {
			Location loc = getLocation(product);
			double costs = result.getDirectCostsOf(product);
			double v = costs == 0 ? 0 : -costs;
			cons.compute(loc,
					(_loc, oldVal) -> oldVal == null ? v : oldVal + v);
		});
		double total = result.getTotalCosts();
		total = total == 0 ? 0 : -total;
		return asContributions(cons, total);
	}

	/**
	 * Calculates location contributions to the total net-costs.
	 */
	public List<Contribution<Location>> getNetCostsContributions() {
		if (result == null)
			return Collections.emptyList();
		HashMap<Location, Double> cons = new HashMap<>();
		result.techIndex().each((i, product) -> {
			Location loc = getLocation(product);
			double v = result.getDirectCostsOf(product);
			cons.compute(loc,
					(_loc, oldVal) -> oldVal == null ? v : oldVal + v);
		});
		double total = result.getTotalCosts();
		return asContributions(cons, total);
	}

	private List<Contribution<Location>> asContributions(
			HashMap<Location, Double> cons, double total) {
		return cons.entrySet().stream().map(e -> {
			var c = new Contribution<Location>();
			c.amount = e.getValue() == null ? 0 : e.getValue();
			c.item = e.getKey();
			c.computeShare(total);
			return c;
		}).collect(Collectors.toList());
	}

	private Location getLocation(TechFlow p) {
		if (p == null || !(p.provider() instanceof ProcessDescriptor d))
			return null;
		return d.location == null
				? null
				: getLocation(d.location);
	}

	private Location getLocation(long id) {
		Location loc = cache.get(id);
		if (loc != null)
			return loc;
		loc = new LocationDao(db).getForId(id);
		cache.put(id, loc);
		return loc;
	}

}
