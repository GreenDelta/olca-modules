package org.openlca.core.model;

public interface Copyable<T extends Copyable<T>> {

	T copy();

	class Copy {
		private Copy() {
		}

		/**
		 * A null-safe method for copying the given element.
		 */
		@SuppressWarnings("unchecked")
		public static <T extends Copyable<? super T>> T of(T copyable) {
			return copyable == null
				? null
				: (T) copyable.copy();
		}
	}
}
