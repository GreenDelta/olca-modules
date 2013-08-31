package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.results.InventoryResult;
import org.openlca.core.results.ProjectResult;

public class ProjectCalculator {

	private IDatabase database;

	public ProjectCalculator(IDatabase database) {
		this.database = database;
	}

	public ProjectResult solve(Project project) {
		ProjectResult result = new ProjectResult();
		SystemCalculator calculator = new SystemCalculator(database);
		for (ProjectVariant v : project.getVariants()) {
			CalculationSetup setup = new CalculationSetup(v.getProductSystem(),
					CalculationSetup.QUICK_RESULT);
			setup.getParameterRedefs().addAll(v.getParameterRedefs());
			InventoryResult inventoryResult = calculator.solve(setup);
			result.addResult(v, inventoryResult);
		}
		return result;
	}
}
