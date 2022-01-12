package org.openlca.ilcd.epd.model;

import java.util.Objects;

public final class Scenario implements Cloneable {

	public String name;
	public boolean defaultScenario;
	public String group;
	public String description;

	@Override
	public String toString() {
		return "Scenario {name=" + name + ", group=" + group + "}";
	}

	@Override
	public Scenario clone() {
		try {
			return (Scenario) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		var other = (Scenario) o;
		return Objects.equals(name, other.name)
				&& Objects.equals(group, other.group);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, group);
	}
}
