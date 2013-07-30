package org.openlca.core.math;

import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public interface IResultData {

	FlowDescriptor[] getFlows();

	ImpactCategoryDescriptor[] getImpactCategories();

	boolean hasImpactResults();

}
