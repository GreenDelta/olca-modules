package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.model.CostCategory;

public final class CostResults {

	private CostResults() {
	}

	public static Set<CostCategory> getCostCategories(CostResult result,
			EntityCache cache) {
		if (result == null)
			return Collections.emptySet();
		Set<CostCategory> set = new HashSet<>();
		if (result.hasVarCostResults()) {
			for (long id : result.getVarCostCategoryIndex().getKeys())
				set.add(cache.get(CostCategory.class, id));
		}
		if (result.hasFixCostResults()) {
			for (long id : result.getFixCostCategoryIndex().getKeys())
				set.add(cache.get(CostCategory.class, id));
		}
		return set;
	}

	public static List<CostCategoryResult> getCostCategoryResults(
			CostResult result, EntityCache cache) {
		if (result == null)
			return Collections.emptyList();
		double[] varResults = result.getVarCostResults();
		LongIndex varIndex = result.getVarCostCategoryIndex();
		double[] fixResults = result.getFixCostResults();
		LongIndex fixIndex = result.getFixCostCategoryIndex();
		List<CostCategoryResult> results = new ArrayList<>();
		for (CostCategory cat : getCostCategories(result, cache)) {
			CostCategoryResult r = new CostCategoryResult();
			results.add(r);
			r.setCostCategory(cat);
			if (cat.fix)
				r.setAmount(fetchVal(cat, fixIndex, fixResults));
			else
				r.setAmount(fetchVal(cat, varIndex, varResults));
		}
		return results;
	}

	private static double fetchVal(CostCategory cat, LongIndex index,
			double[] results) {
		if (cat == null || index == null || results == null)
			return 0;
		int idx = index.getIndex(cat.getId());
		if (idx < 0 || idx >= results.length)
			return 0;
		return results[idx];
	}

}
