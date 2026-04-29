package org.openlca.io.olca.systransfer;

import java.util.List;

import org.openlca.commons.Strings;

/// Strategy for finding matching providers in the target database
/// when a product system is transferred.
public enum MatchingStrategy {

	/// Match providers by their reference ID.
	BY_ID {
		@Override
		ProviderMatch matchOf(
			ProviderInfo provider, List<ProviderInfo> candidates
		) {
			var refId = idOf(provider);
			if (Strings.isBlank(refId))
				return null;

			ProviderInfo selected = null;
			for (var c : candidates) {
				if (Strings.equalsIgnoreCase(refId, idOf(c))) {
					selected = c;
					break;
				}
			}
			return selected != null
				? new ProviderMatch(provider, selected, candidates)
				: null;
		}

		private String idOf(ProviderInfo info) {
			return info != null && info.provider() != null
				? info.provider().refId
				: null;
		}
	},

	/// Match providers by name.
	BY_NAME {
		@Override
		ProviderMatch matchOf(
			ProviderInfo provider, List<ProviderInfo> candidates
		) {
			var name = nameOf(provider);
			ProviderInfo selected = null;
			for (var c : candidates) {
				if (Strings.equalsIgnoreCase(name, nameOf(c))) {
					selected = c;
					break;
				}
			}
			return selected != null
				? new ProviderMatch(provider, selected, candidates)
				: null;
		}

		private String nameOf(ProviderInfo info) {
			var name = info.provider() != null
				? info.provider().name
				: "";
			if (info.location() != null) {
				name += " - " + info.location().code;
			}
			return Strings.isBlank(name) ? "" : name.strip();
		}
	},

	/// Matches the first best provider with the same product as output or waste
	/// as input.
	ANY {
		@Override
		ProviderMatch matchOf(
			ProviderInfo provider, List<ProviderInfo> candidates
		) {
			var selected = candidates.isEmpty()
				? null
				: candidates.getFirst();
			if (selected == null)
				return null;
			return new ProviderMatch(provider, selected, candidates);
		}
	};

	abstract ProviderMatch matchOf(
		ProviderInfo provider, List<ProviderInfo> candidates
	);
}
