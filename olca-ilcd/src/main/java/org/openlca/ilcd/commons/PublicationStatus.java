
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "WorkflowAndPublicationStatusValues")
@XmlEnum
public enum PublicationStatus {

	/**
	 * Data set is in preliminary status of on-going development.
	 * 
	 */
	@XmlEnumValue("Working draft")
	WORKING_DRAFT("Working draft"),

	/**
	 * Data set is finished and ready for internal review.
	 * 
	 */
	@XmlEnumValue("Final draft for internal review")
	FINAL_DRAFT_FOR_INTERNAL_REVIEW("Final draft for internal review"),

	/**
	 * Data set is ready for an external review (after a potential internal
	 * review and correction if required).
	 * 
	 */
	@XmlEnumValue("Final draft for external review")
	FINAL_DRAFT_FOR_EXTERNAL_REVIEW("Final draft for external review"),

	/**
	 * Data set is finalised (with or without an internal and/or external review
	 * and correction if required), but it is not or not yet published.
	 * 
	 */
	@XmlEnumValue("Data set finalised; unpublished")
	DATA_SET_FINALISED_UNPUBLISHED("Data set finalised; unpublished"),

	/**
	 * Data set is under revision and the publication of the revised data set is
	 * foreseen.
	 * 
	 */
	@XmlEnumValue("Under revision")
	UNDER_REVISION("Under revision"),

	/**
	 * Data set has been withdrawn and must not be used anymore. For details
	 * contact the "Data set owner".
	 * 
	 */
	@XmlEnumValue("Withdrawn")
	WITHDRAWN("Withdrawn"),

	/**
	 * Data set is finalised (with or without an internal and/or external review
	 * and correction if required), and sub-system(s) / included processes have
	 * been published.
	 * 
	 */
	@XmlEnumValue("Data set finalised; subsystems published")
	DATA_SET_FINALISED_SUBSYSTEMS_PUBLISHED("Data set finalised; subsystems published"),

	/**
	 * Data set is finalised (with or without an internal and/or external review
	 * and correction if required), and was entirely published.
	 * 
	 */
	@XmlEnumValue("Data set finalised; entirely published")
	DATA_SET_FINALISED_ENTIRELY_PUBLISHED("Data set finalised; entirely published");
	private final String value;

	PublicationStatus(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static PublicationStatus fromValue(String v) {
		for (PublicationStatus c : PublicationStatus.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
