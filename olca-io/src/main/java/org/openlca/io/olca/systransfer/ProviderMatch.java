package org.openlca.io.olca.systransfer;

import java.util.List;
import java.util.Objects;

/// A match of a provider in the source database to a selected provider in the
/// target database with a list of possible alternatives. A match always has a
/// selected provider (as the currently best match) which is also contained in
/// the list of alternatives.
public class ProviderMatch {

	private final ProviderInfo provider;
	private final List<ProviderInfo> alternatives;

	private ProviderInfo selected;
	private MatchingStrategy strategy;

	ProviderMatch(
		ProviderInfo provider,
		List<ProviderInfo> alternatives) {
		this.provider = Objects.requireNonNull(provider);
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

	/// Returns a possible matching strategy with which the matching provider was
	/// selected. This may return `null`, e.g. when it was selected manually.
	public MatchingStrategy strategy() {
		return strategy;
	}

	public void select(ProviderInfo selected) {
		select(selected, null);
	}

	ProviderMatch select(
		ProviderInfo selected, MatchingStrategy strategy
	) {
		if (selected == null) return this;
		if (!alternatives.contains(selected)) {
			throw new IllegalArgumentException(
				"Selected provider must be one of the alternatives");
		}
		this.selected = selected;
		this.strategy = strategy;
		return this;
	}

}
