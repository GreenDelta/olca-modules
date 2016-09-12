
package org.openlca.ilcd.descriptors;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "CompletenessValues", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
@XmlEnum
public enum CompletenessValues {

	/**
	 * All relevant flows are recorded. [Note: This does not consider yet the
	 * quality or representativeness of the data.]
	 * 
	 */
	@XmlEnumValue("All relevant flows quantified")
	ALL_RELEVANT_FLOWS_QUANTIFIED("All relevant flows quantified"),

	/**
	 * One or more relevant flows are not recorded respectively not quantified.
	 * [Note: These flows should be found in the Inputs and Outputs with mean
	 * amount "0" and marked as "Missing relevant" in the field "Data derivation
	 * type/status".]
	 * 
	 */
	@XmlEnumValue("Relevant flows missing")
	RELEVANT_FLOWS_MISSING("Relevant flows missing"),

	/**
	 * Process is not relevant for the environmental impact. E.g. it is a dummy
	 * process or a generic mixer process, or is not contributing to a specified
	 * environmental problem.
	 * 
	 */
	@XmlEnumValue("Topic not relevant")
	TOPIC_NOT_RELEVANT("Topic not relevant"),

	/**
	 * Unclear, not stated.
	 * 
	 */
	@XmlEnumValue("No statement")
	NO_STATEMENT("No statement");
	private final String value;

	CompletenessValues(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static CompletenessValues fromValue(String v) {
		for (CompletenessValues c : CompletenessValues.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
