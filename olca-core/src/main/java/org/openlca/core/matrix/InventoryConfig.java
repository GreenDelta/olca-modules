package org.openlca.core.matrix;

import java.util.Collections;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.results.SimpleResult;
import org.openlca.expressions.FormulaInterpreter;

/**
 * A class for configuration objects that are passed into an inventory builder.
 */
public class InventoryConfig {

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

	private InventoryConfig(Builder builder) {
		this.db = builder.db;
		this.techIndex = builder.techIndex;
		linker = techIndex.hasLinks()
			? techIndex
			: TechLinker.Default.of(techIndex);

		if (builder.setup != null) {
			var setup = builder.setup;
			withUncertainties = setup.withUncertainties;
			withCosts = setup.withCosts;
			withRegionalization = setup.withRegionalization;
			allocationMethod = setup.allocationMethod == null
				? AllocationMethod.NONE
				: setup.allocationMethod;
		} else {
			withUncertainties = false;
			withCosts = false;
			withRegionalization = false;
			allocationMethod = AllocationMethod.NONE;
		}

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
		private CalculationSetup setup;
		private FormulaInterpreter interpreter;
		private Map<ProcessProduct, SimpleResult> subResults;


		private Builder(IDatabase db, TechIndex techIndex) {
			this.db = db;
			this.techIndex = techIndex;
		}

		public Builder withSetup(CalculationSetup setup) {
			this.setup = setup;
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

		public InventoryConfig create() {
			return new InventoryConfig(this);
		}
	}

}
