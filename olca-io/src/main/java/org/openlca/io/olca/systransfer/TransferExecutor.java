package org.openlca.io.olca.systransfer;

import org.openlca.commons.Res;
import org.openlca.core.model.ProductSystem;

public class TransferExecutor {

	private final TransferPlan plan;

	private TransferExecutor(TransferPlan plan) {
		this.plan = plan;
	}

	public static TransferExecutor of(TransferPlan plan) {
		return new TransferExecutor(plan);
	}

	public Res<ProductSystem> execute() {

		// TODO
		// - traverse the product system in the same way as in the TransferPlan.PlanBuilder
		// - while traversing complete the new product system
		// - copy required for foreground processes
		// - link matched providers
		// - autocomplete the system for the matched providers
		// - copy analysis groups, parameter sets (including global parameters)

		return Res.error("Not yet implemented");
	}

}
