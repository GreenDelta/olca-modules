package org.openlca.core.results;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.reader.LibReaderRegistry;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactDescriptor;

/**
 * A project result is basically just a collection of contribution results of
 * the projects' variants.
 */
public class ProjectResult {

	private final HashMap<ProjectVariant, LcaResult> results = new HashMap<>();

	public static Builder of(Project project, IDatabase db) {
		return new Builder(project, db);
	}

	public Set<ProjectVariant> getVariants() {
		return Collections.unmodifiableSet(results.keySet());
	}

	public LcaResult getResult(ProjectVariant variant) {
		return results.get(variant);
	}

	public double getTotalFlowResult(ProjectVariant variant, EnviFlow flow) {
		var r = results.get(variant);
		return r != null && r.hasEnviFlows()
				? r.getTotalFlowValueOf(flow)
				: 0;
	}

	public List<EnviFlowValue> getTotalFlowResults(ProjectVariant variant) {
		var r = results.get(variant);
		return r != null && r.hasEnviFlows()
				? r.getTotalFlows()
				: Collections.emptyList();
	}

	public List<Contribution<ProjectVariant>> getContributions(EnviFlow flow) {
		return Contributions.calculate(
				getVariants(), variant -> getTotalFlowResult(variant, flow));
	}

	public double getTotalImpactResult(
			ProjectVariant variant, ImpactDescriptor impact) {
		var r = results.get(variant);
		return r != null && r.hasImpacts()
				? r.getTotalImpactValueOf(impact)
				: 0;
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

	/**
	 * This method must be called when this result is not needed anymore.
	 */
	public void dispose() {
		for (var r : results.values()) {
			r.dispose();
		}
	}

	public static class Builder {

		private final Project project;
		private final IDatabase db;
		private LibReaderRegistry libraries;
		private MatrixSolver solver;

		private Builder(Project project, IDatabase db) {
			this.project = Objects.requireNonNull(project);
			this.db = Objects.requireNonNull(db);
		}

		public Builder withLibraries(LibReaderRegistry libraries) {
			this.libraries = libraries;
			return this;
		}

		public Builder withLibraries(LibraryDir libDir) {
			if (libDir != null) {
				this.libraries = LibReaderRegistry.of(db, libDir);
			}
			return this;
		}

		public Builder withSolver(MatrixSolver solver) {
			this.solver = solver;
			return this;
		}

		public ProjectResult calculate() {
			var result = new ProjectResult();
			var calculator = new SystemCalculator(db)
					.withLibraries(libraries)
					.withSolver(solver);
			for (var v : project.variants) {
				if (v.isDisabled)
					continue;
				var setup = CalculationSetup.of(v.productSystem)
						.withUnit(v.unit)
						.withFlowPropertyFactor(v.flowPropertyFactor)
						.withAmount(v.amount)
						.withAllocation(v.allocationMethod)
						.withImpactMethod(project.impactMethod)
						.withNwSet(project.nwSet)
						.withParameters(v.parameterRedefs)
						.withCosts(project.isWithCosts)
						.withRegionalization(project.isWithRegionalization);
				var variantResult = calculator.calculate(setup);
				result.results.put(v, variantResult);
			}
			return result;
		}
	}
}
