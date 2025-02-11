package org.openlca.git.util;

import org.openlca.git.model.ModelRef;
import org.openlca.util.TypedRefIdMap;

public class ModelRefMap<T> extends TypedRefIdMap<T> {

	public ModelRefMap<T> put(ModelRef ref, T value) {
		if (ref.isCategory) {
			put(ref.type, ref.path, value);
		} else {
			put(ref.type, ref.refId, value);
		}
		return this;
	}

	public boolean contains(ModelRef ref) {
		if (ref.isCategory)
			return contains(ref.type, ref.path);
		return contains(ref.type, ref.refId);
	}

	public T get(ModelRef ref) {
		if (ref.isCategory)
			return get(ref.type, ref.path);
		return get(ref.type, ref.refId);
	}

	public T remove(ModelRef ref) {
		if (ref.isCategory)
			return remove(ref.type, ref.path);
		return remove(ref.type, ref.refId);
	}

}
