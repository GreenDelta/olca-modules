package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.List;

public class LcaSetup {

	private EntityRef impactMethod;
	private final List<SystemBinding> systemBindings = new ArrayList<>();

	public EntityRef impactMethod() {
		return impactMethod;
	}

	public void impactMethod(EntityRef impactMethod) {
		this.impactMethod = impactMethod;
	}

	public List<SystemBinding> systemBindings() {
		return systemBindings;
	}
}
