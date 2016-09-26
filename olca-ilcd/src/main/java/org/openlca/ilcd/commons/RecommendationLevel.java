
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "RecommendationLevelValues")
@XmlEnum
public enum RecommendationLevel {

	/**
	 * Highest recommendation level. See also field "Specific meaning of the
	 * recommendation level".
	 */
	@XmlEnumValue("Level I")
	LEVEL_I("Level I"),

	/**
	 * Second highest recommendation level. See also field "Specific meaning of
	 * the recommendation level".
	 */
	@XmlEnumValue("Level II")
	LEVEL_II("Level II"),

	/**
	 * Third highest recommendation level. See also field "Specific meaning of
	 * the recommendation level".
	 */
	@XmlEnumValue("Level III")
	LEVEL_III("Level III"),

	/**
	 * Level between the third highest recommendation level and "not
	 * recommended". See also field "Specific meaning of the recommendation
	 * level".
	 */
	@XmlEnumValue("Interim")
	INTERIM("Interim"),

	/**
	 * Not recommended for use.
	 */
	@XmlEnumValue("Not recommended")
	NOT_RECOMMENDED("Not recommended");

	private final String value;

	RecommendationLevel(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static RecommendationLevel fromValue(String v) {
		for (RecommendationLevel c : RecommendationLevel.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
