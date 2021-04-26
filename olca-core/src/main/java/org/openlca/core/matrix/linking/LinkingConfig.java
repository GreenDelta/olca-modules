package org.openlca.core.matrix.linking;

import org.openlca.core.model.ProcessType;

public class LinkingConfig {

	public ProcessType preferredType = ProcessType.LCI_RESULT;
	public ProviderLinking providerLinking = ProviderLinking.PREFER_DEFAULTS;
	public Double cutoff;
	public LinkingCallback callback;
}
