package org.openlca.io.hestia;

public enum MethodTier {

	/// Data refer to emissions which do not occur in the current Cycle. They are
	/// either drawn from external, secondary, and often aggregated datasets such
	/// as ecoinvent or are drawn from a linked Impact Assessment.
	BACKGROUND("background"),

	/// Refers to emissions which are quantified using physical measurement. Terms
	/// to describe the measurement technique are contained in the measurement
	/// glossary.
	MEASURED("measured"),

	/// Models quantify emissions from activity data (i.e., data on Inputs,
	/// Practices, etc.) using a simple equation with parameters which are not
	/// country or region specific.
	TIER_1("tier 1"),

	/// Models quantify emissions from activity data using a simple equation,
	/// often of the same form as the tier 1 model, but with geographically
	/// specific parameters.
	TIER_2("tier 2"),

	/// Models quantify emissions from activity data but use equations or
	/// algorithms that differ from tier 1 and tier 2 approaches. These approaches
	/// include process based models and statistical models with various forms.
	TIER_3("tier 3"),

	/// Means this emission is not relevant and in this case value = 0.
	NOT_RELEVANT("not relevant");

	private final String value;

	MethodTier(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static MethodTier fromString(String s) {
		if (s == null)
			return null;
		var v = s.strip().toLowerCase();
		for (var tier : MethodTier.values()) {
			if (v.equals(tier.value))
				return tier;
		}
		return null;
	}
}
