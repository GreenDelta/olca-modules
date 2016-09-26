
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "TypeOfReviewValues")
@XmlEnum
public enum ReviewType {

	/**
	 * Reviewer(s)/verifier(s) have been involved in collecting, modelling, or
	 * entering the data set information or inventory, have commissioned or
	 * financed the work, or represent the process or product system (or
	 * quantiatitively relevant parts of it).
	 */
	@XmlEnumValue("Dependent internal review")
	DEPENDENT_INTERNAL_REVIEW("Dependent internal review"),

	/**
	 * Reviewer(s)/verifier(s) have NOT been involved in collecting, modelling,
	 * or entering the data set information or inventory, have not commissioned
	 * or financed the work, and do not represent the process or product system
	 * (or quantiatitively relevant parts of it). But the reviewer(s) belong(s)
	 * to the organisation(s) or legally linked organisation(s) that have been
	 * involved in the above.
	 */
	@XmlEnumValue("Independent internal review")
	INDEPENDENT_INTERNAL_REVIEW("Independent internal review"),

	/**
	 * Reviewer(s)/verifier(s) do not belong to the organisations or legally
	 * linked organisations that have been involved in collecting, modelling, or
	 * entering the data set information or inventory, have not commissioned or
	 * financed the work, or represent the process or product system (or
	 * quantiatitively relevant parts of it).
	 */
	@XmlEnumValue("Independent external review")
	INDEPENDENT_EXTERNAL_REVIEW("Independent external review"),

	/**
	 * Reviewer(s)/verifier(s) do not belong to the organisations or legally
	 * linked organisations that have been involved in collecting, modelling, or
	 * entering the data set information or inventory, have not commissioned or
	 * financed the work, or represent the process or product system (or
	 * quantiatitively relevant parts of it). The reviewer(s)/verifier(s) are
	 * accredited by an accreditation body, that is independent of the
	 * reviewer(s)/verifier(s) and the scheme operator or standardisation party.
	 */
	@XmlEnumValue("Accredited third party review")
	ACCREDITED_THIRD_PARTY_REVIEW("Accredited third party review"),

	/**
	 * Panel of at least three independent internal or external
	 * reviewers/verfiers. Chairperson is independent external
	 * reviewer/verifier. Chairperson may invite interested parties affected by
	 * the conclusions drawn from the LCA, such as government agencies,
	 * non-governmental groups, competitors and affected industries. [Notes:
	 * "Independent": Reviewer(s)/verifier(s) have NOT been involved in
	 * collecting, modelling, or entering the data set information or inventory,
	 * have not commissioned or financed the work, and do not represent the
	 * process or product system (or quantiatitively relevant parts of it).
	 * "External": Reviewer(s)/verifier(s) do not belong to the organisations or
	 * legally linked organisations that have been involved in collecting,
	 * modelling, or entering the data set information or inventory, have not
	 * commissioned or financed the work, or represent the process or product
	 * system (or quantiatitively relevant parts of it).
	 */
	@XmlEnumValue("Independent review panel")
	INDEPENDENT_REVIEW_PANEL("Independent review panel"),

	/**
	 * The data set was not / not yet reviewed.
	 */
	@XmlEnumValue("Not reviewed")
	NOT_REVIEWED("Not reviewed");
	private final String value;

	ReviewType(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ReviewType fromValue(String v) {
		for (ReviewType c : ReviewType.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
