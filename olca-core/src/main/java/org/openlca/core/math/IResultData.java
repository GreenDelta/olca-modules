package org.openlca.core.math;

import org.openlca.core.model.Flow;
import org.openlca.core.model.lean.ImpactCategoryDescriptor;

public interface IResultData {

	Flow[] getFlows();

	ImpactCategoryDescriptor[] getImpactCategories();

	boolean hasImpactResults();

}
