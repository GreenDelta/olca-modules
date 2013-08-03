package org.openlca.io;

public class Triple<T1, T2, T3> {

	private T1 first = null;
	private T2 second = null;
	private T3 third = null;

	public Triple(final T1 first, final T2 second, final T3 third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public T1 getFirst() {
		return first;
	}

	public T2 getSecond() {
		return second;
	}

	public T3 getThird() {
		return third;
	}

	public void setFirst(final T1 first) {
		this.first = first;
	}

	public void setSecond(final T2 second) {
		this.second = second;
	}

	public void setThird(final T3 third) {
		this.third = third;
	}
}