package org.openlca.core.matrix;

import java.util.Collections;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.results.SimpleResult;
import org.openlca.expressions.FormulaInterpreter;

public class MatrixConfig {

	public final IDatabase db;
	public final TechIndex techIndex;
	public final TechLinker linker;

	public final boolean withUncertainties;
	public final boolean withCosts;
	public final boolean withRegionalization;
	public final AllocationMethod allocationMethod;

	/**
	 * Optional sub-system results of the product system.
	 */
	public final Map<ProcessProduct, SimpleResult> subResults;
	public final FormulaInterpreter interpreter;

	private MatrixConfig(Builder builder) {
		this.db = builder.db;
		this.techIndex = builder.techIndex;
		linker = techIndex.hasLinks()
			? techIndex
			: TechLinker.Default.of(techIndex);

		withUncertainties = builder.withUncertainties;
		withCosts = builder.withCosts;
		withRegionalization = builder.withRegionalization;
		allocationMethod = builder.allocationMethod == null
			? AllocationMethod.NONE
			: builder.allocationMethod;

		interpreter = builder.interpreter != null
			? builder.interpreter
			: new FormulaInterpreter();
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

	public static class Builder {
		private final IDatabase db;
		private final TechIndex techIndex;

		private FormulaInterpreter interpreter;
		private Map<ProcessProduct, SimpleResult> subResults;
		private AllocationMethod allocationMethod;
		public boolean withUncertainties;
		public boolean withCosts;
		public boolean withRegionalization;

		private Builder(IDatabase db, TechIndex techIndex) {
			this.db = db;
			this.techIndex = techIndex;
		}

		public Builder withSetup(CalculationSetup setup) {
			if (setup != null) {
				withUncertainties = setup.withUncertainties;
				withCosts = setup.withCosts;
				withRegionalization = setup.withRegionalization;
				allocationMethod = setup.allocationMethod;
			}
			return this;
		}

		public Builder withUncertainties() {
			withUncertainties = true;
			return this;
		}

		public Builder withCosts() {
			withCosts = true;
			return this;
		}

		public Builder withRegionalization() {
			withRegionalization = true;
			return this;
		}

		public Builder withAllocation(AllocationMethod method) {
			allocationMethod = method;
			return this;
		}

		public Builder withInterpreter(FormulaInterpreter interpreter) {
			this.interpreter = interpreter;
			return this;
		}

		public Builder withSubResults(Map<ProcessProduct, SimpleResult> results) {
			this.subResults = results;
			return this;
		}

		public MatrixConfig create() {
			return new MatrixConfig(this);
		}
	}

}
