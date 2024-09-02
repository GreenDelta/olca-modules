package org.openlca.core.model;

import java.util.Locale;
import java.util.Optional;

public enum EpdType {

	GENERIC_DATASET("generic dataset"),
	REPRESENTATIVE_DATASET("representative dataset"),
	AVERAGE_DATASET("average dataset"),
	SPECIFIC_DATASET("specific dataset"),
	TEMPLATE_DATASET("template dataset");

	private final String label;

	EpdType(String label) {
		this.label = label;
	}

	public static Optional<EpdType> fromString(String s) {
		if (s == null)
			return Optional.empty();
		var lo = s.strip().toLowerCase(Locale.US);
		for (var v : values()) {
			if (lo.equals(v.label))
				return Optional.of(v);
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return label;
	}
}
