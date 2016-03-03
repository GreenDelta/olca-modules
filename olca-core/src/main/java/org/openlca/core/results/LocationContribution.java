package org.openlca.core.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.EntityCache;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * Calculates the contributions of the single process results in an analysis
 * result grouped by their locations.
 */
public class LocationContribution {

	private ContributionResultProvider<?> result;
	private Map<Location, List<ProcessDescriptor>> index = new HashMap<>();

	public LocationContribution(ContributionResultProvider<?> result) {
		this.result = result;
		initProcessIndex();
	}

	private void initProcessIndex() {
		if (result == null)
			return;
		EntityCache cache = result.cache;
		for (ProcessDescriptor process : result.getProcessDescriptors()) {
			Location loc = null;
			if (process.getLocation() != null)
				loc = cache.get(Location.class, process.getLocation());
			List<ProcessDescriptor> list = index.get(loc);
			if (list == null) {
				list = new ArrayList<>();
				index.put(loc, list);
			}
			list.add(process);
		}
	}

	/** Calculates contributions to an inventory flow. */
	public ContributionSet<Location> calculate(FlowDescriptor flow) {
		if (flow == null || result == null)
			return ContributionSet.empty();
		double total = result.getTotalFlowResult(flow).value;
		return Contributions.calculate(index.keySet(), total, location -> {
			double amount = 0;
			for (ProcessDescriptor p : index.get(location))
				amount += result.getSingleFlowResult(p, flow).value;
			return amount;
		});
	}

	/** Calculates contributions to an impact category. */
	public ContributionSet<Location> calculate(ImpactCategoryDescriptor impact) {
		if (impact == null || result == null)
			return ContributionSet.empty();
		double total = result.getTotalImpactResult(impact).value;
		return Contributions.calculate(index.keySet(), total, location -> {
			double amount = 0;
			for (ProcessDescriptor p : index.get(location))
				amount += result.getSingleImpactResult(p, impact).value;
			return amount;
		});
	}

	/** Calculates added values aggregated by location. */
	public ContributionSet<Location> addedValues() {
		if (result == null)
			return ContributionSet.empty();
		double total = result.getTotalCostResult();
		total = total == 0 ? 0 : -total;
		return Contributions.calculate(index.keySet(), total, location -> {
			double amount = 0;
			for (ProcessDescriptor p : index.get(location)) {
				double r = result.getSingleCostResult(p);
				r = r == 0 ? 0 : -r;
				amount += r;
			}
			return amount;
		});
	}

	/** Calculates net-costs aggregated by location. */
	public ContributionSet<Location> netCosts() {
		if (result == null)
			return ContributionSet.empty();
		double total = result.getTotalCostResult();
		return Contributions.calculate(index.keySet(), total, location -> {
			double amount = 0;
			for (ProcessDescriptor p : index.get(location)) {
				amount += result.getSingleCostResult(p);
			}
			return amount;
		});
	}

}
