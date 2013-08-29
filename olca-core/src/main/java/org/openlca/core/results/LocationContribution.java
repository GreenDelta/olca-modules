package org.openlca.core.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.Cache;
import org.openlca.core.matrices.LongPair;
import org.openlca.core.matrices.ProductIndex;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * Calculates the contributions of the single process results in an analysis
 * result grouped by their locations.
 */
public class LocationContribution {

	private AnalysisResult result;
	private Map<Location, List<ProcessDescriptor>> processIndex = new HashMap<>();
	private Location defaultLocation;

	public LocationContribution(AnalysisResult result, String defaultName,
			Cache cache) {
		this.result = result;
		defaultLocation = new Location();
		defaultLocation.setCode(defaultName);
		defaultLocation.setName(defaultName);
		defaultLocation.setRefId(UUID.randomUUID().toString());
		initProcessIndex(cache);
	}

	private void initProcessIndex(Cache cache) {
		if (result == null || result.getProductIndex() == null)
			return;
		ProductIndex index = result.getProductIndex();
		for (int i = 0; i < index.size(); i++) {
			LongPair processProduct = index.getProductAt(i);
			ProcessDescriptor p = cache.getProcessDescriptor(processProduct
					.getFirst());
			Location loc = p.getLocation() == null ? defaultLocation : cache
					.getLocation(p.getLocation());
			List<ProcessDescriptor> list = processIndex.get(loc);
			if (list == null) {
				list = new ArrayList<>();
				processIndex.put(loc, list);
			}
			list.add(p);
		}
	}

	/** Calculates contributions to an inventory flow. */
	public ContributionSet<Location> calculate(FlowDescriptor flow) {
		if (flow == null || result == null)
			return ContributionSet.empty();
		List<Contribution<Location>> contributions = new ArrayList<>();
		for (Location loc : processIndex.keySet()) {
			List<ProcessDescriptor> list = processIndex.get(loc);
			double amount = 0;
			for (ProcessDescriptor p : list)
				amount += result.getSingleFlowResult(p.getId(), flow.getId());
			Contribution<Location> contribution = new Contribution<>();
			contribution.setAmount(amount);
			contribution.setItem(loc);
			contributions.add(contribution);
		}
		ContributionShare.calculate(contributions);
		return new ContributionSet<>(contributions);
	}

	/** Calculates contributions to an impact assessment method. */
	public ContributionSet<Location> calculate(ImpactCategoryDescriptor impact) {
		if (impact == null || result == null)
			return ContributionSet.empty();
		List<Contribution<Location>> contributions = new ArrayList<>();
		for (Location loc : processIndex.keySet()) {
			List<ProcessDescriptor> list = processIndex.get(loc);
			double amount = 0;
			for (ProcessDescriptor p : list)
				amount += result.getSingleImpactResult(p.getId(),
						impact.getId());
			Contribution<Location> contribution = new Contribution<>();
			contribution.setAmount(amount);
			contribution.setItem(loc);
			contributions.add(contribution);
		}
		ContributionShare.calculate(contributions);
		return new ContributionSet<>(contributions);
	}

}
