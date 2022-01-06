package org.openlca.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Computes a canonical form of a compartment path. For example, 'Elementary
 * flows/Emissions to air/unspecified' is reduced to 'air'.
 */
public class CompartmentStemmer {

	private final String[] EMPTY = new String[0];
	private final HashMap<Key, String[]> cache = new HashMap<>();
	private final Map<String, String> replacements;
	private final Set<String> filter;


	public CompartmentStemmer() {
		filter = new HashSet<>();
		var filterWords = List.of(
			"elementary",
			"emission",
			"emissions",
			"flows",
			"in",
			"from",
			"of",
			"to",
			"and",
			"or",
			"unspecified"
		);
		filter.addAll(filterWords);

		replacements = new HashMap<>();
		replacements.put("resources", "resource");
		replacements.put("emissions", "emission");
	}

	public String[] stem(String path) {
		return path == null || path.isBlank()
			? EMPTY
			: stem(path.split("/"));
	}

	public String[] stem(String... path) {
		if (path == null || path.length == 0)
			return EMPTY;
		var key = new Key(path);
		var cached = cache.get(key);
		if (cached != null)
			return cached;

		var segments = new ArrayList<Segment>();
		for (var next : path) {
			append(segments, next);
		}
		if (segments.isEmpty())
			return EMPTY;

		var stemmed = segments.stream()
			.map(Segment::toString)
			.toArray(String[]::new);
		cache.put(key, stemmed);
		return stemmed;
	}

	private void append(List<Segment> prefix, String next) {
		if (Strings.nullOrEmpty(next))
			return;
		var words = new HashSet<String>();
		var word = new StringBuilder();

		Runnable handleNext = () -> {
			if (word.length() == 0)
				return;
			var s = word.toString().toLowerCase();
			word.setLength(0);
			s = replacements.getOrDefault(s, s);
			if (filter.contains(s))
				return;
			for (var previous : prefix) {
				if (previous.words.contains(s))
					return;
			}
			words.add(s);
		};

		for (var c : next.toCharArray()) {
			if (Character.isAlphabetic(c)) {
				word.append(c);
				continue;
			}
			handleNext.run();
		}
		handleNext.run();

		if (!words.isEmpty()) {
			prefix.add(new Segment(words));
		}
	}

	private record Key(String[] content) {

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Key other = (Key) o;
			return Arrays.equals(content, other.content);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(content);
		}
	}

	private record Segment(HashSet<String> words) {

		@Override
		public String toString() {
			var sorted = new ArrayList<>(words);
			Collections.sort(sorted);
			return Strings.join(sorted, ',');
		}
	}
}
