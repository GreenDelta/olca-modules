package org.openlca.text;

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

	private final Set<String> stopWords;
	private final Set<String> skippedPhrases;
	private final Map<String, String> phraseReplacements;
	private final Map<String, String> wordReplacements;

	public CompartmentStemmer() {
		stopWords = new HashSet<>();
		stopWords.addAll(List.of(
			"in",
			"from",
			"of",
			"to",
			"and",
			"or",
			"emission"
		));

		skippedPhrases = new HashSet<>();
		skippedPhrases.addAll(List.of(
			"elementary flows",
			"emission",
			"unspecified"
		));

		wordReplacements = new HashMap<>();
		wordReplacements.put("resources", "resource");
		wordReplacements.put("emissions", "emission");
		wordReplacements.put("fossilwater", "fossil");
		wordReplacements.put("groundwater", "ground");
		wordReplacements.put("airborne", "air");
		wordReplacements.put("waterborne", "water");
		wordReplacements.put("ground-", "ground");

		phraseReplacements = new HashMap<>();
		phraseReplacements.put("emissions to air", "air");
		phraseReplacements.put("high population density", "high population");
		phraseReplacements.put("high. pop.", "high population");
		phraseReplacements.put("low population density", "low population");
		phraseReplacements.put("low. pop.", "low population");
		phraseReplacements.put("economic issues", "economic");
		phraseReplacements.put("social issues", "social");
		phraseReplacements.put("raw", "resource");
		phraseReplacements.put("final waste flows", "waste");
		phraseReplacements.put("raw materials", "resource");
		phraseReplacements.put("non material emissions", "non material");
		phraseReplacements.put("non mat.", "non material");
		phraseReplacements.put("land use", "resource");
		phraseReplacements.put("high stacks", "high stack");
		phraseReplacements.put("natural resource", "resource");
		phraseReplacements.put("fossil well", "fossil");
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
		if (next == null || next.isBlank())
			return;

		var feed = next.trim().toLowerCase();
		var phrases = new HashSet<String>();
		var wordBuffer = new StringBuilder();
		var phraseBuffer = new StringBuilder();

		Runnable phraseEnded = () -> {
			if (phraseBuffer.isEmpty())
				return;
			var phrase = read(phraseBuffer, phraseReplacements);
			if (skippedPhrases.contains(phrase))
				return;
			for (var previous : prefix) {
				if (previous.phrases.contains(phrase))
					return;
			}
			phrases.add(phrase);
		};

		Runnable wordEnded = () -> {
			if (wordBuffer.isEmpty())
				return;
			var word = read(wordBuffer, wordReplacements);

			// end the phrase if it is a stop word
			if (stopWords.contains(word)) {
				phraseEnded.run();
				return;
			}

			// skip the word if it appeared before
			for (var previous : prefix) {
				if (previous.phrases.contains(word)) {
					phraseEnded.run();
					return;
				}
			}

			if (phraseBuffer.length() > 0) {
				phraseBuffer.append(' ');
			}
			phraseBuffer.append(word);
		};

		for (var c : feed.toCharArray()) {
			if (isWordChar(c)) {
				wordBuffer.append(c);
				continue;
			}
			if (Character.isWhitespace(c)) {
				wordEnded.run();
				continue;
			}
			wordEnded.run();
			phraseEnded.run();
		}
		wordEnded.run();
		phraseEnded.run();


		if (!phrases.isEmpty()) {
			prefix.add(new Segment(phrases));
		}
	}

	private String read(StringBuilder builder, Map<String, String> repl) {
		var s = builder.toString();
		builder.setLength(0);
		return repl.getOrDefault(s, s);
	}

	private boolean isWordChar(char c) {
		return Character.isAlphabetic(c)
			|| c == '-' || c == '.';
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

	private record Segment(HashSet<String> phrases) {

		@Override
		public String toString() {
			var sorted = new ArrayList<>(phrases);
			Collections.sort(sorted);
			return String.join(";", sorted);
		}
	}
}
