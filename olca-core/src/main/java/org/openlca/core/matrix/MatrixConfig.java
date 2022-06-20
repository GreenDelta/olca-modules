package org.openlca.core.matrix;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.CalculationType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.results.SimpleResult;
import org.openlca.expressions.FormulaInterpreter;

public class MatrixConfig {

	public final IDatabase db;
	public final TechIndex techIndex;
	public final TechLinker linker;
	public final Demand demand;

	public final boolean withUncertainties;
	public final boolean withCosts;
	public final boolean withRegionalization;
	public final AllocationMethod allocationMethod;

	/**
	 * Optional sub-system results of the product system.
	 */
	public final Map<TechFlow, SimpleResult> subResults;
	public final ImpactIndex impactIndex;
	public final FormulaInterpreter interpreter;

	private MatrixConfig(Builder builder) {
		this.db = builder.db;
		this.demand = builder.demand;
		this.techIndex = builder.techIndex;
		linker = techIndex.hasLinks()
			? techIndex
			: TechLinker.Default.of(techIndex);
		impactIndex = builder.impacts != null
			? builder.impacts
			: ImpactIndex.empty();

		// build the formula interpreter
		var contexts = new HashSet<>(techIndex.getProcessIds());
		if (!impactIndex.isEmpty()) {
			impactIndex.content().forEach(
				impact -> contexts.add(impact.id));
		}
		Collection<ParameterRedef> redefs = builder.redefs != null
			? builder.redefs
			: Collections.emptyList();
		interpreter = ParameterTable.interpreter(
			db, contexts, redefs);

		// optional settings
		withUncertainties = builder.withUncertainties;
		withCosts = builder.withCosts;
		withRegionalization = builder.withRegionalization;
		allocationMethod = builder.allocationMethod == null
			? AllocationMethod.NONE
			: builder.allocationMethod;
		subResults = builder.subResults != null
			? builder.subResults
			: Collections.emptyMap();
	}

	public static Builder of(IDatabase db, TechIndex techIndex) {
		return new Builder(db, techIndex);
	}

	boolean hasAllocation() {
		return allocationMethod != null
					 && allocationMethod != AllocationMethod.NONE;
	}

	boolean hasImpacts() {
		return !impactIndex.isEmpty();
	}

	public static class Builder {

		private final IDatabase db;
		private final TechIndex techIndex;
		private Demand demand;
		private ImpactIndex impacts;
		private List<ParameterRedef> redefs;
		private Map<TechFlow, SimpleResult> subResults;

		private AllocationMethod allocationMethod;
		private boolean withUncertainties;
		private boolean withCosts;
		private boolean withRegionalization;

		private Builder(IDatabase db, TechIndex techIndex) {
			this.db = db;
			this.techIndex = techIndex;
		}

		public Builder withSetup(CalculationSetup setup) {
			if (setup == null)
				return this;
			demand = Demand.of(setup);
			withCosts = setup.hasCosts();
			withRegionalization = setup.hasRegionalization();
			allocationMethod = setup.allocation();
			redefs = setup.parameters();
			withUncertainties = setup.type() == CalculationType.MONTE_CARLO_SIMULATION;
			var impactMethod = setup.impactMethod();
			return impactMethod != null
				? withImpacts(ImpactIndex.of(impactMethod))
				: this;
		}

		public Builder withDemand(Demand demand) {
			this.demand = demand;
			return this;
		}

		public Builder withUncertainties(boolean b) {
			withUncertainties = b;
			return this;
		}

		public Builder withCosts(boolean b) {
			withCosts = b;
			return this;
		}

		public Builder withRegionalization(boolean b) {
			withRegionalization = b;
			return this;
		}

		public Builder withAllocation(AllocationMethod method) {
			allocationMethod = method;
			return this;
		}

		public Builder withImpacts(ImpactIndex impacts) {
			if (impacts != null && !impacts.isEmpty()) {
				this.impacts = impacts;
			}
			return this;
		}

		public Builder withParameterRedefs(List<ParameterRedef> redefs) {
			this.redefs = redefs;
			return this;
		}

		public Builder withSubResults(Map<TechFlow, SimpleResult> results) {
			this.subResults = results;
			return this;
		}

		public MatrixData build() {
			var conf = new MatrixConfig(this);
			var data = new InventoryBuilder(conf).build();
			// add the LCIA matrix structures; note that in case
			// of a library system we may not have elementary
			// flows in the foreground system but still want to
			// attach an impact index to the matrix data.
			if (conf.hasImpacts()) {
				if (EnviIndex.isEmpty(data.enviIndex)) {
					data.impactIndex = conf.impactIndex;
				} else {
					ImpactBuilder.of(conf, data.enviIndex)
						.build()
						.addTo(data);
				}
			}
			return data;
		}
	}

}
