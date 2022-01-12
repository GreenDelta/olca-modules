package org.openlca.ilcd.epd.model;

import java.util.Objects;

public class ModuleEntry {

	public Module module;
	public String scenario;
	public String description;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof ModuleEntry other))
			return false;
		return Objects.equals(this.module, other.module)
				&& Objects.equals(this.scenario, other.scenario);
	}

	@Override
	public int hashCode() {
		return Objects.hash(module, scenario);
	}

	@Override
	public String toString() {
		return "ModuleEntry [module=" + module + ", scenario=" + scenario + "]";
	}

	@Override
	public ModuleEntry clone() {
		ModuleEntry clone = new ModuleEntry();
		clone.module = module;
		clone.scenario = scenario;
		clone.description = description;
		return clone;
	}
}
