package org.openlca.core.matrix.io.npy;

/**
 * An enumeration of the Numpy data types that we currently support in our
 * NPY format implementation.
 * <p>
 * see https://docs.scipy.org/doc/numpy/reference/arrays.dtypes.html
 */
public enum DType {

	/**
	 * 64 bit floating point numbers.
	 */
	Float64,

	/**
	 * Signed 32 bit integers.
	 */
	Int32,

	/**
	 * Signed 64 bit integers.
	 */
	Int64,

	/**
	 * All other things that we currently not support.
	 */
	UNKNOWN;

	/**
	 * Try to derive the data type from the given Numpy dtype string; returns
	 * UNKNOWN otherwise.
	 */
	public static DType fromString(String dtype) {
		if (dtype == null)
			return UNKNOWN;
		if (dtype.endsWith("f8"))
			return Float64;
		if (dtype.endsWith("i4"))
			return Int32;
		if (dtype.endsWith("i8"))
			return Int64;
		return UNKNOWN;
	}

	/**
	 * Returns the number of bytes that are used to store an instance of the
	 * respective data type.
	 */
	public int size() {
		switch (this) {
			case Float64:
			case Int64:
				return 8;
			case Int32:
				return 4;
			default:
				return -1;
		}
	}
}
