package org.openlca.core.model.results;

import org.openlca.core.math.Index;
import org.openlca.core.model.CostCategory;

public class SimpleCostResult {

	private Index<CostCategory> costCategoryIndex;
	private double[] results;

	public SimpleCostResult(Index<CostCategory> costCategoryIndex,
			double[] results) {
		this.costCategoryIndex = costCategoryIndex;
		this.results = results;
	}

	public CostCategory[] getCostCategories() {
		if (costCategoryIndex == null)
			return new CostCategory[0];
		return costCategoryIndex.getItems();
	}

	public double getResult(CostCategory costCategory) {
		if (costCategoryIndex == null || results == null)
			return Double.NaN;
		int idx = costCategoryIndex.getIndex(costCategory);
		if (idx < 0 || idx >= results.length)
			return Double.NaN;
		return results[idx];
	}

	public void appendFixCosts(CostCategory[] costCategories, double[] values) {
		if (costCategories == null || values == null
				|| costCategories.length == 0 || values.length == 0)
			return;
		if (costCategories.length != values.length)
			throw new IllegalArgumentException(
					"Nr. of cost categories do not match nr. of values");

		double[] tmp = new double[results.length + values.length];
		for (int i = 0; i < tmp.length; i++)
			if (i < results.length)
				tmp[i] = results[i];
			else
				tmp[i] = values[i - results.length];
		results = tmp;

		for (CostCategory costCategory : costCategories)
			costCategoryIndex.put(costCategory);
	}
}
