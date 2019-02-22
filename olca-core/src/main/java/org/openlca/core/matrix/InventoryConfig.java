package org.openlca.core.matrix;

import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.results.SimpleResult;
import org.openlca.expressions.FormulaInterpreter;

/**
 * A class for configuration objects that are passed into an inventory builder.
 */
public class InventoryConfig {

	public final IDatabase db;
	public final TechIndex techIndex;

	public boolean withUncertainties;
	public AllocationMethod allocationMethod;
	public Map<ProcessProduct, SimpleResult> subResults;
	public FormulaInterpreter interpreter;

	public InventoryConfig(IDatabase db, TechIndex techIndex) {
		this.db = db;
		this.techIndex = techIndex;
	}

}
