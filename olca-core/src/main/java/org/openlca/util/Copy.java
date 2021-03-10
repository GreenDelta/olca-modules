package org.openlca.util;

import org.openlca.core.model.Copyable;

public final class Copy {

	private Copy() {
	}

	/**
	 * A null-safe method for copying the given element. **NOTE:** this method
	 * only works if the copy method of T returns an instance or a sub-type of T.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Copyable<? super T>> T of(T copyable) {
		return copyable == null
			? null
			: (T) copyable.copy();
	}
}
