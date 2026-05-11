package org.openlca.io.olca.systransfer;

import java.util.List;

import org.openlca.commons.Strings;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.text.PhraseParser;
import org.openlca.text.PhraseSimilarity;

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


	},

	/// Matches the first best provider with the same product as output or waste
	/// as input. If there are multiple possible matching providers, the "first
	/// best" is selected by the similarity of the name.
	ANY {
		@Override
		ProviderMatch matchOf(
			ProviderInfo provider, List<ProviderInfo> candidates
		) {
			if (candidates.isEmpty())
				return null;
			if (candidates.size() == 1)
				return new ProviderMatch(provider, candidates.getFirst(), candidates);

			var parser = new PhraseParser();
			var similarity = new PhraseSimilarity();
			var words = parser.parse(nameOf(provider));

			ProviderInfo selected = null;
			double score = 0;
			for (var c : candidates) {
				double nextScore = similarity.get(words, parser.parse(nameOf(c)));
				if (selected == null || nextScore > score) {
					selected = c;
					score = nextScore;
				}
			}

			return selected != null
				? new ProviderMatch(provider, selected, candidates)
				: null;
		}
	};

	abstract ProviderMatch matchOf(
		ProviderInfo provider, List<ProviderInfo> candidates
	);

	private static String nameOf(ProviderInfo info) {
		if (info == null) return "";
		var name = canonicalNameOf(info.provider());
		if (info.location() != null) {
			name += " - " + info.location().code;
		}
		return Strings.isBlank(name) ? "" : name.strip();
	}

	private static String canonicalNameOf(RootDescriptor provider) {
		if (provider == null || Strings.isBlank(provider.name))
			return "";

		var name = provider.name.strip().toLowerCase();
		if (name.isEmpty())
			return "";
		if (name.endsWith(", u") || name.endsWith(", s")) {
			name = name.substring(0, name.length() - 3);
		}

		var buffer = new StringBuilder();
		for (var part : name.split("\\|")) {
			var segment = part.strip();
			if (skipSegment(segment))
				continue;
			if (!buffer.isEmpty()) {
				buffer.append(" | ");
			}
			buffer.append(segment);
		}
		return buffer.toString();
	}

	private static boolean skipSegment(String segment) {
		// TODO: filter all possible system models
		return "apos".equals(segment)
			|| "cutoff".equals(segment);
	}
}
