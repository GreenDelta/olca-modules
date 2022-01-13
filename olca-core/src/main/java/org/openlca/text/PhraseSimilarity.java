package org.openlca.text;

import java.util.Set;

public class PhraseSimilarity {

	private final WordMatrix matrix;
	private Set<String> negations;
	private SemanticFactorProvider semanticFactors;

	public PhraseSimilarity() {
		matrix = new WordMatrix();
	}

	public PhraseSimilarity negations(Set<String> negations) {
		this.negations = negations;
		return this;
	}

	public PhraseSimilarity semanticFactors(SemanticFactorProvider fn) {
		this.semanticFactors = fn;
		return this;
	}

	public double get(WordBuffer phrase1, WordBuffer phrase2) {
		if (phrase1.size() == 0 && phrase2.size() == 0)
			return 1;
		if (phrase1.size() == 0 || phrase2.size() == 0)
			return 0;

		matrix.reset(phrase1, phrase2);
		boolean negated1 = false;
		boolean negated2 = false;

		for (int row = 0; row < phrase1.size(); row++) {
			var word1 = phrase1.get(row);

			// handle negation in s1
			if (isNegation(word1)) {
				if (!negated1) {
					negated1 = true;
				}
				continue;
			}

			for (int col = 0; col < phrase2.size(); col++) {
				var word2 = phrase2.get(col);

				// handle negation in s2
				if (isNegation(word2)) {
					if (!negated2) {
						negated2 = true;
					}
					continue;
				}
				if (negated1 != negated2)
					continue;

				double f = wordFactor(word1, word2) * distanceFactor(row, col);
				matrix.set(row, col, f);
			}
		}

		return matrix.similarity();
	}

	private boolean isNegation(String word) {
		return negations != null && negations.contains(word);
	}

	private double wordFactor(String word1, String word2) {
		if (word1.equalsIgnoreCase(word2))
			return 1.0;
		return semanticFactors != null
			? semanticFactors.get(word1, word2)
			: 0.0;
	}

	private double distanceFactor(int pos1, int pos2) {
		if (pos1 == pos2)
			return 1.0;
		double dist = Math.abs(pos1 - pos2);
		return 1.0 / Math.log10(10.0 * (1.0 + dist * 0.3));
	}

	@FunctionalInterface
	public interface SemanticFactorProvider {
		double get(String w1, String w2);
	}

}
