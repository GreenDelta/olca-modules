package org.openlca.text;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

class WordMatrix {

	record Entry(
		int row,
		int col,
		double factor,
		double rowMatch,
		double colMatch,
		double totalMatch) {
	}

	private Entry[] entries;
	private WordBuffer rows;
	private WordBuffer cols;
	private int entryCount;
	private int dim;

	private Entry[] sortedEntries;
	private Entry[] work;
	private Entry[] candidates;
	private boolean[] handledRows;
	private boolean[] handledCols;
	private final Comparator<Entry> comparator = Comparator.comparingDouble(
		(Entry e) -> e.totalMatch).reversed();

	void reset(WordBuffer rows, WordBuffer cols) {
		this.rows = Objects.requireNonNull(rows);
		this.cols = Objects.requireNonNull(cols);
		dim = rows.size() * cols.size();
		entries = clear(entries, dim);
		entryCount = 0;
	}

	void set(int i, int j, double factor) {
		if (factor == 0)
			return;
		var rowWord = rows.get(i);
		double rowMatch = factor * (double) rowWord.length();
		var colWord = cols.get(j);
		double colMatch = factor * (double) colWord.length();
		var entry = new Entry(i, j, factor,
			rowMatch, colMatch, rowMatch + colMatch);
		int idx = i * cols.size() + j;
		entries[idx] = entry;
		entryCount++;
	}

	double similarity() {
		if (entryCount == 0)
			return 0;

		sortedEntries = clear(sortedEntries, entryCount);
		int k = 0;
		for (int i = 0; i < dim; i++) {
			var entry = entries[i];
			if (entry != null) {
				sortedEntries[k] = entry;
				k++;
			}
		}
		Arrays.sort(sortedEntries, 0, entryCount, comparator);

		int rowCount = rows.size();
		int colCount = cols.size();
		work = clear(work, rowCount);
		candidates = clear(candidates, rowCount);
		handledRows = clear(handledRows, rowCount);
		handledCols = clear(handledCols, colCount);

		double maxSum = 0;
		double candidatesCount = 0;
		for (int i = 0; i < entryCount; i++) {
			var start = sortedEntries[i];
			Arrays.fill(handledRows, 0, rowCount, false);
			Arrays.fill(handledCols, 0, colCount, false);
			handledRows[start.row] = true;
			handledCols[start.col] = true;
			work[0] = start;
			double sum = start.totalMatch;
			int n = 1;

			for (int j = 0; j < entryCount; j++) {
				if (n == rowCount)
					break;
				if (i == j)
					continue;
				var next = sortedEntries[j];
				if (handledRows[next.row] || handledCols[next.col])
					continue;
				handledRows[next.row] = true;
				handledCols[next.col] = true;
				work[n] = next;
				n++;
				sum += next.totalMatch;
			}

			if (sum > maxSum) {
				maxSum = sum;
				candidatesCount = n;
				var temp = candidates;
				candidates = work;
				work = temp;
			}
		}

		if (maxSum == 0)
			return 0;

		double totalRowLen = totalLen(rows);
		double totalColLen = totalLen(cols);
		double matchedRowLen = 0;
		double matchedColLen = 0;
		for (int i = 0; i < candidatesCount; i++) {
			var e = candidates[i];
			matchedRowLen += e.rowMatch;
			matchedColLen += e.colMatch;
		}

		return totalRowLen == 0 || totalColLen == 0
			? 0
			: 0.5 * ((matchedRowLen / totalRowLen) + (matchedColLen / totalColLen));
	}

	private Entry[] clear(Entry[] entries, int size) {
		if (entries == null || entries.length < size) {
			int len = size + (int) Math.round((double) size * 0.3);
			return new Entry[len];
		}
		Arrays.fill(entries, 0, size, null);
		return entries;
	}

	private boolean[] clear(boolean[] values, int size) {
		if (values == null || values.length < size) {
			int len = size + (int) Math.round((double) size * 0.3);
			return new boolean[len];
		}
		Arrays.fill(values, 0, size, false);
		return values;
	}

	private double totalLen(WordBuffer buffer) {
		double len = 0;
		for (int i = 0; i < buffer.size(); i++) {
			len += buffer.get(i).length();
		}
		return len;
	}

}
