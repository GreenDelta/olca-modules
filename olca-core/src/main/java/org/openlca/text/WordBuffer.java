package org.openlca.text;

import java.util.ArrayList;

public final class WordBuffer {

	private final ArrayList<String> buffer;

	public WordBuffer() {
		this(10);
	}

	public WordBuffer(int capacity) {
		buffer = new ArrayList<>(capacity);
	}

	int size() {
		return buffer.size();
	}

	boolean isEmpty() {
		return buffer.isEmpty();
	}

	String get(int i) {
		return buffer.get(i);
	}

	void add(String word) {
		buffer.add(word);
	}

	void reset() {
		if (buffer.isEmpty())
			return;
		buffer.clear();
	}

	@Override
	public String toString() {
		return buffer.toString();
	}

	public String[] toArray() {
		return buffer.toArray(String[]::new);
	}
}
