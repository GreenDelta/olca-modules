package org.openlca.core.matrix;

/**
 * Maps a set of instances of a given type to the corresponding rows or columns
 * of a matrix.
 */
public interface MatrixIndex<T> {

	/**
	 * Get the index element at the given matrix position.
	 */
	T at(int i);

	/**
	 * Get the matrix position of the given index element. Returns -1 if the
	 * given element is not part of this index.
	 */
	int of(T elem);

	/**
	 * Returns true if this index contains the given element.
	 */
	default boolean contains(T elem) {
		if (elem == null)
			return false;
		return of(elem) >= 0;
	}

	/**
	 * Returns the number of elements of this index.
	 */
	int size();

	/**
	 * Returns true when this index is empty and does not contain any element.
	 */
	default boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Iterates over this index calling the given function for each position and
	 * element pair.
	 */
	void each(IndexConsumer<T> fn);

	/**
	 * Creates a copy of this index. Note that this does not copy the elements
	 * of the index.
	 */
	MatrixIndex<T> copy();
}
