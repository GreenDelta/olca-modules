package org.openlca.sd.interop;

import org.openlca.core.model.ParameterRedef;
import org.openlca.sd.model.Id;

public class VarBinding {

	private ParameterRedef parameter;
	private Id varId;

	public ParameterRedef parameter() {
		return parameter;
	}

	public VarBinding parameter(ParameterRedef parameter) {
		this.parameter = parameter;
		return this;
	}

	public Id varId() {
		return varId;
	}

	public VarBinding varId(Id varId) {
		this.varId = varId;
		return this;
	}
}
