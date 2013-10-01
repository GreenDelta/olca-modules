package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of possible boundaries with nature
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public enum BoundaryWithNature {

	/**
	 * Agricultural production is part of natural systems
	 */
	AGRICULTURAL_NATURAL_SYSTEM(
			"Agricultural production is part of natural systems"),

	/**
	 * Agricultural production is part of production system
	 */
	AGRICULTURAL_PRODUCTION_SYSTEM(
			"Agricultural production is part of production system"),

	/**
	 * No boundary applicable
	 */
	NOT_APPLICABLE("Not applicable"),

	/**
	 * Unknown boundary
	 */
	UNKNOWN("Unknown"),

	/**
	 * Unspecified boundary
	 */
	UNSPECIFIED("Unspecified");

	private String value;

	private BoundaryWithNature(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static BoundaryWithNature forValue(String value) {
		BoundaryWithNature boundaryWithNature = null;
		int i = 0;
		while (boundaryWithNature == null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				boundaryWithNature = values()[i];
			} else {
				i++;
			}
		}
		return boundaryWithNature;
	}

}
