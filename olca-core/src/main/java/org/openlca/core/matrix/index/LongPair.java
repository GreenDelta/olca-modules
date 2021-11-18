package org.openlca.core.matrix.index;

/**
 * A LongPair saves just a pair of longs. We use this typically to efficiently
 * store the IDs of a process and a product or waste flow as pair that we then
 * use in indices to build matrix structures for the calculation. In this case,
 * the first value should be always the process ID and the second the flow ID.
 */
public record LongPair(long first, long second)
	implements Comparable<LongPair> {

	public static LongPair of(long first, long second) {
		return new LongPair(first, second);
	}

	@Override
	public String toString() {
		return "LongPair[first=" + first + ", second=" + second + "]";
	}

	@Override
	public int hashCode() {
		return hashCode(first, second);
	}

	public static int hashCode(long first, long second) {
		long h = first * 79 + second;
		return (int) ((h >> 32) ^ h);
	}

	/**
	 * Returns true if the given values are exactly the same as those of this pair.
	 */
	public boolean equals(long first, long second) {
		return this.first == first && this.second == second;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		return obj instanceof LongPair other
			&& this.first == other.first
			&& this.second == other.second;
	}

	@Override
	public int compareTo(LongPair other) {
		if (other == null)
			return 1;
		int c = Long.compare(this.first, other.first);
		if (c != 0)
			return c;
		return Long.compare(this.second, other.second);
	}

}
