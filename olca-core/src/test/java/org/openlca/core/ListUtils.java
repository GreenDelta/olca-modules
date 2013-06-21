package org.openlca.core;

import java.util.List;
import java.util.Objects;

import org.openlca.core.model.descriptors.BaseDescriptor;

public class ListUtils {

	/**
	 * Returns the descriptor with the given ID from the given list, or null if
	 * no such descriptor is contained in the list
	 */
	public static <T extends BaseDescriptor> T findDescriptor(String id,
			List<T> descriptors) {
		if (descriptors == null)
			return null;
		for (T descriptor : descriptors) {
			if (Objects.equals(id, descriptor.getId()))
				return descriptor;
		}
		return null;
	}

}
