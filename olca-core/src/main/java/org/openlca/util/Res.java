package org.openlca.util;

import java.util.Objects;
import java.util.function.Supplier;

/// `Res` is a generic result type that contains a value, an error, or is empty
/// (`VOID`). This is useful for API calls that should not throw exceptions but
/// return an optional error value instead.
public final class Res<T> {

	private final T value;
	private final String error;
	public static final Res<Void> VOID = new Res<>(null, null);

	private Res(T value, String error) {
		this.value = value;
		this.error = error;
	}

	public static <T> Res<T> of(T value) {
		return new Res<>(Objects.requireNonNull(value), null);
	}

	@SuppressWarnings("unchecked")
	public static <T> Res<T> ofNullable(T value) {
		return value != null
				? new Res<>(value, null)
				: (Res<T>) VOID;
	}

	public static <T> Res<T> error(String message) {
		return new Res<>(null, message);
	}

	public static <T> Res<T> error(String message, Throwable e) {
		return e != null
				? error(message + ": " + e.getMessage())
				: error(message);
	}

	public T value() {
		return value;
	}

	public String error() {
		return error;
	}

	public boolean hasValue() {
		return value != null;
	}

	public boolean hasError() {
		return error != null;
	}

	public boolean isVoid() {
		return this == VOID;
	}

	@SuppressWarnings("unchecked")
	public <E> Res<E> castError() {
		if (error != null && value == null)
			return (Res<E>) this;
		return error != null
				? error(error)
				: error("no error message");
	}

	/**
	 * Packs the given error message on top of the current error and
	 * returns that combined error.
	 */
	public <E> Res<E> wrapError(String outerErr) {
		return error != null
				? error(outerErr + "\n\t" + error)
				: error(outerErr);
	}

	public T orElse(Supplier<T> fn) {
		return !hasError() && value != null
				? value
				: fn.get();
	}

	public T orElseThrow() {
		if (error != null)
			throw new IllegalStateException("Val has an error: " + error);
		if (value == null && !isVoid())
			throw new NullPointerException("Val has a null value");
		return value;
	}
}

