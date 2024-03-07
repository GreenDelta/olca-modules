package org.openlca.git.util;

import java.util.Collection;

import org.openlca.git.model.ModelRef;

public class ModelRefSet extends TypedRefIdSet {

	public ModelRefSet() {
		super();
	}

	public ModelRefSet(Collection<? extends ModelRef> refs) {
		addAll(refs);
	}

	public void add(ModelRef ref) {
		if (ref.isCategory) {
			add(ref.type, ref.path);
		} else {
			add(ref.type, ref.refId);
		}
	}

	public boolean contains(ModelRef ref) {
		if (ref.isCategory)
			return contains(ref.type, ref.path);
		return contains(ref.type, ref.refId);
	}

	public void remove(ModelRef ref) {
		if (ref.isCategory) {
			remove(ref.type, ref.path);
		} else {
			remove(ref.type, ref.refId);
		}
	}
}
