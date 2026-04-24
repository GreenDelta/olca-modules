package org.openlca.io.olca.systransfer;

import java.util.List;
import java.util.Objects;

/// A match of a provider in the source database to a selected provider in the
/// target database with a list of possible alternatives. A match always has a
/// selected provider (as the currently best match) which is also contained in
/// the list of alternatives.
public class ProviderMatch {

	private final ProviderInfo provider;
	private ProviderInfo selected;
	private List<ProviderInfo> alternatives;

	ProviderMatch(
		ProviderInfo provider,
		ProviderInfo selected,
		List<ProviderInfo> alternatives) {
		this.provider = Objects.requireNonNull(provider);
		this.selected = Objects.requireNonNull(selected);
		this.alternatives = Objects.requireNonNull(alternatives);
	}

	public ProviderInfo provider() {
		return provider;
	}

	public ProviderInfo selected() {
		return selected;
	}

	public List<ProviderInfo> alternatives() {
		return alternatives;
	}

	public void select(ProviderInfo selected) {
		if (!alternatives.contains(selected)) {
			throw new IllegalArgumentException(
				"Selected provider must be one of the alternatives");
		}
		this.selected = selected;
	}

}
