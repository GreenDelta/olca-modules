package org.openlca.io.smartepd;

import java.util.Optional;

public enum SmartMethod {

	IPCC_AR5_GWP100("IPCC AR5 GWP 100"),

	IPCC_AR5_GWP50("IPCC AR5 GWP 50"),

	IPCC_AR5_GWP20("IPCC AR5 GWP 20"),

	IPCC_AR6_GWP100("IPCC AR6 GWP 100"),

	TRACI_2_1("TRACI 2.1"),

	AWARE("AWARE"),

	RECIPE_2016("ReCiPe 2016 v1.1"),

	CML_2016("CML 2016 v4.8"),

	EF_3_0("EF3.0"),

	EF_3_1("EF3.1");

	private final String id;

	SmartMethod(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public static Optional<SmartMethod> of(String id) {
		for (var m : values()) {
			if (m.id.equalsIgnoreCase(id))
				return Optional.of(m);
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return id;
	}
}
