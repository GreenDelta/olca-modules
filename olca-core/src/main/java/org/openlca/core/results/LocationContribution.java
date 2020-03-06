package org.openlca.core.results;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.AtomicDouble;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * Calculates the contributions of the locations to a result.
 */
public class LocationContribution {

	private final IDatabase db;
	private final ContributionResult result;
	private final TLongObjectHashMap<Location> cache = new TLongObjectHashMap<>();

	public LocationContribution(ContributionResult result, IDatabase db) {
		this.result = result;
		this.db = db;
	}

	/**
	 * Calculates contributions by location to the given inventory flow.
	 */
	public List<Contribution<Location>> getContributions(FlowDescriptor flow) {
		if (flow == null || result == null)
			return Collections.emptyList();

		HashMap<Location, Double> cons = new HashMap<>();
		double total;
		if (!result.flowIndex.isRegionalized) {
			// non-regionalized calculation;
			// the flow is mapped to a single row
			// we take the locations from the processes
			// in the columns
			int idx = result.flowIndex.of(flow);
			IndexFlow iFlow = result.flowIndex.at(idx);
			if (iFlow == null)
				return Collections.emptyList();
			total = result.getTotalFlowResult(iFlow);
			result.techIndex.each((i, product) -> {
				Location loc = getLocation(product);
				double v = result.getDirectFlowResult(product, iFlow);
				cons.compute(loc,
						(_loc, oldVal) -> oldVal == null ? v : oldVal + v);
			});

		} else {
			// regionalized calculation;
			// the flow is mapped to multiple rows where
			// each row specifies the location
			AtomicDouble t = new AtomicDouble();
			result.flowIndex.each((i, iFlow) -> {
				if (!Objects.equals(flow, iFlow.flow))
					return;
				Location loc = iFlow.location == null
						? null
						: getLocation(iFlow.location.id);
				double v = result.getTotalFlowResult(iFlow);
				t.addAndGet(v);
				cons.compute(loc,
						(_loc, oldVal) -> oldVal == null ? v : oldVal + v);
			});
			total = t.get();
		}

		return asContributions(cons, total);
	}

	/**
	 * Calculates contributions to an impact category.
	 */
	public List<Contribution<Location>> getContributions(
			ImpactCategoryDescriptor impact) {
		if (impact == null || result == null)
			return Collections.emptyList();

		HashMap<Location, Double> cons = new HashMap<>();
		double total = result.getTotalImpactResult(impact);

		if (!result.flowIndex.isRegionalized) {
			// non-regionalized calculation;
			// we take the locations from the processes
			// in the columns and the results from the
			// corresponding process contributions
			result.techIndex.each((i, product) -> {
				Location loc = getLocation(product);
				double v = result.getDirectImpactResult(product, impact);
				cons.compute(loc,
						(_loc, oldVal) -> oldVal == null ? v : oldVal + v);
			});

		} else {
			// regionalized calculation;
			// we take the location from the index flows
			// and the values from the direct contributions
			// of these flows to the LCIA category result
			result.flowIndex.each((i, iFlow) -> {
				Location loc = iFlow.location == null
						? null
						: getLocation(iFlow.location.id);
				double v = result.getDirectFlowImpact(iFlow, impact);
				cons.compute(loc,
						(_loc, oldVal) -> oldVal == null ? v : oldVal + v);
			});

		}

		return asContributions(cons, total);
	}

	/**
	 * Calculates added values aggregated by location.
	 */
	public List<Contribution<Location>> addedValues() {
		if (result == null)
			return Collections.emptyList();
		double total = result.totalCosts;
		total = total == 0 ? 0 : -total;

		return Contributions.calculate(index.keySet(), total, location -> {
			double amount = 0;
			for (CategorizedDescriptor p : index.get(location)) {
				double r = result.getDirectCostResult(p);
				r = r == 0 ? 0 : -r;
				amount += r;
			}
			return amount;
		});
	}

	/**
	 * Calculates net-costs aggregated by location.
	 */
	public List<Contribution<Location>> netCosts() {
		if (result == null)
			return Collections.emptyList();
		double total = result.totalCosts;
		return Contributions.calculate(index.keySet(), total, location -> {
			double amount = 0;
			for (CategorizedDescriptor p : index.get(location)) {
				amount += result.getDirectCostResult(p);
			}
			return amount;
		});
	}

	private List<Contribution<Location>> asContributions(
			HashMap<Location, Double> cons, double total) {
		return cons.entrySet().stream().map(e -> {
			Contribution<Location> c = new Contribution<>();
			c.amount = e.getValue() == null ? 0 : e.getValue();
			c.item = e.getKey();
			c.share = total == 0 ? 0 : c.amount / total;
			return c;
		}).collect(Collectors.toList());
	}

	private Location getLocation(ProcessProduct p) {
		if (p == null || !(p.process instanceof ProcessDescriptor))
			return null;
		ProcessDescriptor d = (ProcessDescriptor) p.process;
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
