package org.openlca.git.util;

import org.openlca.core.model.ModelType;
import org.openlca.git.model.ModelRef;

public class ModelRefMap<T> extends TypedRefIdMap<T> {

	public void put(ModelRef ref, T value) {
		if (ref.isCategory) {
			put(ModelType.CATEGORY, ref.path, value);
		} else {
			put(ref.type, ref.refId, value);
		}
	}

	public boolean contains(ModelRef ref) {
		if (ref.isCategory)
			return contains(ModelType.CATEGORY, ref.path);
		return contains(ref.type, ref.refId);
	}

	public T get(ModelRef ref) {
		if (ref.isCategory)
			return get(ModelType.CATEGORY, ref.path);
		return get(ref.type, ref.refId);
	}

	public T remove(ModelRef ref) {
		if (ref.isCategory)
			return remove(ModelType.CATEGORY, ref.path);
		return remove(ref.type, ref.refId);
	}

}
