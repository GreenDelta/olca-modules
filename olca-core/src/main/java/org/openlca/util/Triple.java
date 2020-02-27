package org.openlca.util;

import java.util.Objects;

public class Triple<F, S, T> {

	public F first;
	public S second;
	public T third;

	public Triple(F first, S second, T third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public static <F, S, T> Triple<F, S, T> of(F first, S second, T third) {
		return new Triple<>(first, second, third);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
		return Objects.equals(first, triple.first) &&
				Objects.equals(second, triple.second) &&
				Objects.equals(third, triple.third);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second, third);
	}

	@Override
	public String toString() {
		return "Triple{first=" + first
				+ ", second=" + second + ", third=" + third + '}';
	}
}
