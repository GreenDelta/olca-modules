package org.openlca.core;

import java.util.List;

import org.openlca.core.model.descriptors.Descriptor;

public class ListUtils {

	/**
	 * Returns the descriptor with the given ID from the given list, or null if
	 * no such descriptor is contained in the list
	 */
	public static <T extends Descriptor> T findDescriptor(long id,
			List<T> descriptors) {
		if (descriptors == null)
			return null;
		for (T descriptor : descriptors) {
			if (id == descriptor.id)
				return descriptor;
		}
		return null;
	}

}
