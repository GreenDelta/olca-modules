package org.openlca.core.services;

import java.util.function.Function;

public record Response<T>(T value, String error) {

	public static <T> Response<T> empty() {
		return new Response<>(null, null);
	}

	public static <T> Response<T> of(T value) {
		return new Response<>(value, null);
	}

	public static <T> Response<T> error(String err) {
		return new Response<>(null, err);
	}

	public static <T> Response<T> error(Exception e) {
		return new Response<>(null, e.getMessage());
	}

	public boolean isValue() {
		return value != null;
	}

	public boolean isEmpty() {
		return value == null && error == null;
	}

	public boolean isError() {
		return error != null;
	}

	public <Q> Response<Q> map(Function<T, Q> fn) {
		if (isEmpty())
			return Response.empty();
		if (isError())
			return Response.error(error);
		return Response.of(fn.apply(value));
	}

}
