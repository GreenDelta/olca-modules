package org.openlca.core.model.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * Calculates the contributions of the single process results in an analysis
 * result grouped by their locations.
 */
public class LocationContribution {

	private AnalysisResult result;
	private Map<Location, List<Process>> processIndex = new HashMap<>();
	private Location defaultLocation;

	public LocationContribution(AnalysisResult result, String defaultName) {
		this.result = result;
		defaultLocation = new Location();
		defaultLocation.setCode(defaultName);
		defaultLocation.setName(defaultName);
		defaultLocation.setRefId(UUID.randomUUID().toString());
		initProcessIndex();
	}

	private void initProcessIndex() {
		if (result == null || result.getSetup().getProductSystem() == null)
			return;
		for (Process p : result.getSetup().getProductSystem().getProcesses()) {
			Location loc = p.getLocation() == null ? defaultLocation : p
					.getLocation();
			List<Process> list = processIndex.get(loc);
			if (list == null) {
				list = new ArrayList<>();
				processIndex.put(loc, list);
			}
			list.add(p);
		}
	}

	/** Calculates contributions to an inventory flow. */
	public ContributionSet<Location> calculate(Flow flow) {
		if (flow == null || result == null)
			return ContributionSet.empty();
		List<Contribution<Location>> contributions = new ArrayList<>();
		for (Location loc : processIndex.keySet()) {
			List<Process> list = processIndex.get(loc);
			double amount = 0;
			for (Process p : list)
				amount += result.getSingleResult(p, flow);
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
			List<Process> list = processIndex.get(loc);
			double amount = 0;
			for (Process p : list)
				amount += result.getSingleResult(p, impact);
			Contribution<Location> contribution = new Contribution<>();
			contribution.setAmount(amount);
			contribution.setItem(loc);
			contributions.add(contribution);
		}
		ContributionShare.calculate(contributions);
		return new ContributionSet<>(contributions);
	}

}
