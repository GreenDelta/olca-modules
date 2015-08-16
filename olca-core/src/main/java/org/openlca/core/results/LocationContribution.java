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
import org.openlca.core.results.Contributions.Function;

/**
 * Calculates the contributions of the single process results in an analysis
 * result grouped by their locations.
 */
public class LocationContribution {

	private ContributionResultProvider<?> result;
	private Map<Location, List<ProcessDescriptor>> processIndex = new HashMap<>();

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
			List<ProcessDescriptor> list = processIndex.get(loc);
			if (list == null) {
				list = new ArrayList<>();
				processIndex.put(loc, list);
			}
			list.add(process);
		}
	}

	/** Calculates contributions to an inventory flow. */
	public ContributionSet<Location> calculate(final FlowDescriptor flow) {
		if (flow == null || result == null)
			return ContributionSet.empty();
		double total = result.getTotalFlowResult(flow).value;
		return Contributions.calculate(processIndex.keySet(), total,
				new Function<Location>() {
					@Override
					public double value(Location loc) {
						List<ProcessDescriptor> list = processIndex.get(loc);
						double amount = 0;
						for (ProcessDescriptor p : list)
							amount += result.getSingleFlowResult(p, flow).value;
						return amount;
					}
				});
	}

	/** Calculates contributions to an impact assessment method. */
	public ContributionSet<Location> calculate(
			final ImpactCategoryDescriptor impact) {
		if (impact == null || result == null)
			return ContributionSet.empty();
		double total = result.getTotalImpactResult(impact).value;
		return Contributions.calculate(processIndex.keySet(), total,
				new Function<Location>() {
					@Override
					public double value(Location loc) {
						List<ProcessDescriptor> list = processIndex.get(loc);
						double amount = 0;
						for (ProcessDescriptor p : list)
							amount += result.getSingleImpactResult(p, impact).value;
						return amount;
					}
				});
	}

}
