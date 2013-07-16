package org.openlca.core.model.lean;

import org.openlca.core.model.ModelType;

/**
 * The descriptor class for impact assessment methods.
 */
public class ImpactMethodDescriptor extends BaseDescriptor {

	private static final long serialVersionUID = 7475077805551284454L;

	public ImpactMethodDescriptor() {
		setType(ModelType.IMPACT_METHOD);
	}

}
