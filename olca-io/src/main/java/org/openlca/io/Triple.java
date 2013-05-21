package org.openlca.io;

/**
 * Wrapper of the objects
 * 
 * @author Sebastian Greve
 * 
 * @param <T1>
 *            The first object
 * @param <T2>
 *            The second object
 * @param <T3>
 *            The third object
 */
public class Triple<T1, T2, T3> {

	/**
	 * The first object
	 */
	private T1 first = null;

	/**
	 * The second object
	 */
	private T2 second = null;

	/**
	 * The third object
	 */
	private T3 third = null;

	/**
	 * Creates a new triple
	 * 
	 * @param first
	 *            The first object
	 * @param second
	 *            The second object
	 * @param third
	 *            The third object
	 */
	public Triple(final T1 first, final T2 second, final T3 third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	/**
	 * Getter of the first object
	 * 
	 * @return The first object
	 */
	public T1 getFirst() {
		return first;
	}

	/**
	 * Getter of the first object
	 * 
	 * @return The first object
	 */
	public T2 getSecond() {
		return second;
	}

	/**
	 * Getter of the third object
	 * 
	 * @return The third object
	 */
	public T3 getThird() {
		return third;
	}

	/**
	 * Setter of the first object
	 * 
	 * @param first
	 *            The first object
	 */
	public void setFirst(final T1 first) {
		this.first = first;
	}

	/**
	 * Setter of the second object
	 * 
	 * @param second
	 *            The second object
	 */
	public void setSecond(final T2 second) {
		this.second = second;
	}

	/**
	 * Setter of the third object
	 * 
	 * @param third
	 *            The third object
	 */
	public void setThird(final T3 third) {
		this.third = third;
	}
}