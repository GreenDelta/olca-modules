package org.openlca.ilcd.epd.model;

import java.util.Objects;

public class Amount {

	public Module module;
	public String scenario;
	public Double value;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Amount other))
			return false;
		return Objects.equals(this.module, other.module)
				&& Objects.equals(this.scenario, other.scenario)
				&& Objects.equals(this.value, other.value);
	}

	@Override
	public Amount clone() {
		Amount clone = new Amount();
		clone.module = module;
		clone.scenario = scenario;
		clone.value = value;
		return clone;
	}
}
