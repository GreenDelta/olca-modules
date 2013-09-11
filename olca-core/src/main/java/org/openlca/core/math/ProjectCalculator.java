package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
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
		ImpactMethodDescriptor method = getImpactMethod(project);
		NormalizationWeightingSet nwSet = getNwSet(project);
		for (ProjectVariant v : project.getVariants()) {
			CalculationSetup setup = new CalculationSetup(v.getProductSystem(),
					CalculationSetup.QUICK_RESULT);
			setup.setImpactMethod(method);
			setup.setNwSet(nwSet);
			setup.getParameterRedefs().addAll(v.getParameterRedefs());
			InventoryResult inventoryResult = calculator.solve(setup);
			result.addResult(v, inventoryResult);
		}
		return result;
	}

	private ImpactMethodDescriptor getImpactMethod(Project project) {
		if (project.getImpactMethodId() == null)
			return null;
		ImpactMethodDao dao = new ImpactMethodDao(database);
		return dao.getDescriptor(project.getImpactMethodId());
	}

	private NormalizationWeightingSet getNwSet(Project project) {
		if (project.getNwSetId() == null)
			return null;
		return database.createDao(NormalizationWeightingSet.class).getForId(
				project.getNwSetId());
	}
}
