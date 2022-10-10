package org.openlca.core.services;

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

	<Q> Response<Q> repack() {
		return Response.error(error);
	}

}