package org.openlca.text;

import java.util.HashSet;
import java.util.Set;

public class PhraseParser {

	private final HashSet<String> stopWords;
	private final HashSet<String> fillSet;

	public PhraseParser() {
		this(null);
	}

	public PhraseParser(Set<String> stopWords) {
		if (stopWords == null || stopWords.isEmpty()) {
			this.stopWords = null;
		} else {
			this.stopWords = new HashSet<>();
			for (var w : stopWords) {
				if (w == null || w.isBlank())
					continue;
				var trimmed = w.trim().toLowerCase();
				this.stopWords.add(trimmed);
			}
		}
		this.fillSet = new HashSet<>();
	}

	/**
	 * Parses the words of the given phrase into a new word buffer.
	 */
	public WordBuffer parse(String phrase) {
		var buffer = new WordBuffer();
		parseInto(buffer, phrase);
		return buffer;
	}

	/**
	 * Resets the given buffer and parses the phrase into the buffer.
	 */
	public void parseInto(WordBuffer buffer, String phrase) {
		fillSet.clear();
		buffer.reset();

		if (phrase == null || phrase.isBlank())
			return;

		var feed = phrase.trim().toLowerCase();
		var word = new StringBuilder();
		Runnable wordEnd = () -> {
			if (word.isEmpty())
				return;
			var w = word.toString().toLowerCase();
			word.setLength(0);
			if (fillSet.contains(w) || isStopWord(w))
				return;
			fillSet.add(w);
			buffer.add(w);
		};

		for (char c : feed.toCharArray()) {
			if (Character.isLetterOrDigit(c)
				|| c == '-' || c == '%') {
				word.append(c);
				continue;
			}
			wordEnd.run();
		}
		wordEnd.run();

	}

	private boolean isStopWord(String word) {
		return stopWords != null && stopWords.contains(word);
	}

}
