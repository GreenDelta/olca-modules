package org.openlca.core.matrix;

import org.openlca.core.matrix.product.index.LinkingMethod;
import org.openlca.core.model.ProcessType;

public class LinkingConfig {

	public ProcessType preferredType = ProcessType.LCI_RESULT;
	public LinkingMethod providerLinking = LinkingMethod.PREFER_PROVIDERS;
	public Double cutoff;

}
