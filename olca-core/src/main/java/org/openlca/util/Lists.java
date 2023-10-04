package org.openlca.util;

import java.util.List;
import java.util.Optional;

public final class Lists {

	private Lists() {
	}

	public static boolean isEmpty(List<?> list) {
		return list == null || list.isEmpty();
	}

	public static <T> Optional<T> first(List<T> list) {
		if (list == null || list.isEmpty())
			return Optional.empty();
		var first = list.get(0);
		return Optional.ofNullable(first);
	}

}
