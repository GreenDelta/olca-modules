package org.openlca.git.actions;

import org.openlca.core.model.ModelType;
import org.openlca.git.model.ModelRef;

public class ConflictException extends RuntimeException {

	private static final long serialVersionUID = -6048314745581027571L;

	public ConflictException(ModelRef ref) {
		this(ref.type, ref.refId);
	}

	public ConflictException(ModelType type, String refId) {
		super("No resolution for conflict with model " + type.name() + " " + refId);
	}

}
