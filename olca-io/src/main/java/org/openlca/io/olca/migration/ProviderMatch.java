package org.openlca.io.olca.migration;

import java.util.List;
import java.util.Objects;

/// A match of a provider in the source database to a selected provider in the
/// target database with a list of possible alternatives. A match always has a
/// selected provider (as the currently best match) which is also contained in
/// the list of alternatives.
public class ProviderMatch {

	private final ProviderInfo source;
	private final List<ProviderInfo> alternatives;

	private ProviderInfo selected;
	private MatchingStrategy strategy;

	ProviderMatch(ProviderInfo source, List<ProviderInfo> alternatives) {
		this.source = Objects.requireNonNull(source);
		this.alternatives = Objects.requireNonNull(alternatives);
	}

	/// The provider information of the source database.
	public ProviderInfo source() {
		return source;
	}

	/// The information of the selected provider in the target database.
	public ProviderInfo selected() {
		return selected;
	}

	/// Returns the full list of possible providers in the target database.
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

	/// Returns `true` if the match is complete, which means it has a valid
	/// provider from the source database and a valid selected provider
	public boolean isComplete() {
		return source.provider() != null
			&& source.flow() != null
			&& selected != null
			&& selected.provider() != null
			&& selected.flow() != null;
	}

}
