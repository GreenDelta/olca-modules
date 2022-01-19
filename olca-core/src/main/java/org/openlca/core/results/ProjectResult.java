package org.openlca.core.results;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactDescriptor;

/**
 * A project result is basically just a collection of contribution results of
 * the projects' variants.
 */
public class ProjectResult {

	private final HashMap<ProjectVariant, ContributionResult> results = new HashMap<>();

	public static ProjectResult calculate(Project project, IDatabase db) {
		var result = new ProjectResult();
		if (project == null)
			return result;
		var calculator = new SystemCalculator(db);
		for (var v : project.variants) {
			if (v.isDisabled)
				continue;
			var setup = CalculationSetup.contributions(v.productSystem)
				.withUnit(v.unit)
				.withFlowPropertyFactor(v.flowPropertyFactor)
				.withAmount(v.amount)
				.withAllocation(v.allocationMethod)
				.withImpactMethod(project.impactMethod)
				.withNwSet(project.nwSet)
				.withParameters(v.parameterRedefs)
				.withCosts(project.isWithCosts)
				.withRegionalization(project.isWithRegionalization);
			var variantResult = calculator.calculateContributions(setup);
			result.results.put(v, variantResult);
		}
		return result;
	}

	public Set<ProjectVariant> getVariants() {
		return Collections.unmodifiableSet(results.keySet());
	}

	public ContributionResult getResult(ProjectVariant variant) {
		return results.get(variant);
	}

	public double getTotalFlowResult(ProjectVariant variant, EnviFlow flow) {
		ContributionResult r = results.get(variant);
		if (r == null)
			return 0;
		return r.getTotalFlowResult(flow);
	}

	public List<FlowValue> getTotalFlowResults(ProjectVariant variant) {
		ContributionResult result = results.get(variant);
		if (result == null)
			return Collections.emptyList();
		return result.getTotalFlowResults();
	}

	public List<Contribution<ProjectVariant>> getContributions(EnviFlow flow) {
		return Contributions.calculate(
			getVariants(), variant -> getTotalFlowResult(variant, flow));
	}

	public double getTotalImpactResult(
		ProjectVariant variant, ImpactDescriptor impact) {
		ContributionResult result = results.get(variant);
		if (result == null)
			return 0;
		return result.getTotalImpactResult(impact);
	}

	public List<Contribution<ProjectVariant>> getContributions(
		ImpactDescriptor impact) {
		return Contributions.calculate(getVariants(),
			variant -> getTotalImpactResult(variant, impact));
	}

	public boolean hasImpacts() {
		for (var result : results.values()) {
			if (result.hasImpacts())
				return true;
		}
		return false;
	}

	public boolean hasCosts() {
		for (var result : results.values()) {
			if (result.hasCosts())
				return true;
		}
		return false;
	}

	public boolean hasEnviFlows() {
		for (var result : results.values()) {
			if (result.hasEnviFlows())
				return true;
		}
		return false;
	}
}
