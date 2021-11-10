package org.openlca.core.matrix.linking;

import java.util.OptionalDouble;

import org.openlca.core.model.ProcessType;

public class LinkingConfig {

	private ProcessType preferredType = ProcessType.LCI_RESULT;
	private ProviderLinking providerLinking = ProviderLinking.PREFER_DEFAULTS;
	private Double cutoff;
	private LinkingCallback callback;

	public ProcessType preferredType() {
		return preferredType;
	}

	public LinkingConfig preferredType(ProcessType type) {
		if (type != null) {
			this.preferredType = type;
		}
		return this;
	}

	public ProviderLinking providerLinking() {
		return providerLinking;
	}

	public LinkingConfig providerLinking(ProviderLinking linking) {
		if (linking != null) {
			this.providerLinking = linking;
		}
		return this;
	}

	public OptionalDouble cutoff() {
		return cutoff == null || cutoff == 0
			? OptionalDouble.empty()
			: OptionalDouble.of(cutoff);
	}

	public LinkingConfig cutoff(Double cutoff) {
		this.cutoff = cutoff;
		return this;
	}

	public LinkingCallback callback() {
		return callback;
	}

	public LinkingConfig callback(LinkingCallback callback) {
		this.callback = callback;
		return this;
	}
}
