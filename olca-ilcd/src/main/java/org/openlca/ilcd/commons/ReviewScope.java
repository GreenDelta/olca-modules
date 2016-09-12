
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ScopeOfReviewValues")
@XmlEnum
public enum ReviewScope {

	/**
	 * Review/verification is done on basis of the original "raw data", i.e. the
	 * data before it was scaled, converted, or treated in other ways to be used
	 * for modelling of a unit process.
	 */
	@XmlEnumValue("Raw data")
	RAW_DATA("Raw data"),

	/**
	 * The review is done on the level of the "Unit operation type unit
	 * process(es)" that can not be further subdivided. Covers multi-functional
	 * processes of unit operation type.
	 */
	@XmlEnumValue("Unit process(es), single operation")
	UNIT_PROCESS_ES_SINGLE_OPERATION("Unit process(es), single operation"),

	/**
	 * The review is done on the level of process-chain(s) or plant level unit
	 * process(es). This covers horizontally averaged unit processes across
	 * different sites. Covers also those multi-functional unit processes, where
	 * the different co-products undergo different processing steps within the
	 * black box.
	 */
	@XmlEnumValue("Unit process(es), black box")
	UNIT_PROCESS_ES_BLACK_BOX("Unit process(es), black box"),

	/**
	 * The LCI result or Partly terminated system is the level of
	 * review/verification.
	 * 
	 */
	@XmlEnumValue("LCI results or Partly terminated system")
	LCI_RESULTS_OR_PARTLY_TERMINATED_SYSTEM("LCI results or Partly terminated system"),

	/**
	 * The LCIA results of the LCI result or Partly terminated system data set
	 * are reviewed/verified, i.e. on level of Climate Change potential, Primary
	 * energy consumption, Ecosystem damage etc. [Note: see also definition for
	 * entry "LCIA results calculation".]
	 * 
	 */
	@XmlEnumValue("LCIA results")
	LCIA_RESULTS("LCIA results"),

	/**
	 * The reporting/documentation of the process or product system, i.e. the
	 * data set's sections "Process description", Modelling and validation" and
	 * "Administrative information" have been reviewed, while this does not
	 * include the "Inputs/Outputs". This can include a review of detailed
	 * background reports.
	 * 
	 */
	@XmlEnumValue("Documentation")
	DOCUMENTATION("Documentation"),

	/**
	 * The application of the LCI method(s) in accordance to the goal and scope
	 * have been reviewed. This covers data collection including dealing with
	 * missing data, data calculation/modelling principles (e.g. consequential
	 * or attributional or other/combination), and the application of the
	 * related modelling approaches such as allocation and system expansion etc.
	 * for the process / throughout the product system.
	 */
	@XmlEnumValue("Life cycle inventory methods")
	LIFE_CYCLE_INVENTORY_METHODS("Life cycle inventory methods"),

	/**
	 * The selection and application of the LCIA method(s) that have been used
	 * for calculation of the LCIA results have been reviewed. This especially
	 * refers to a correspondance of the elementary flows in the Inputs and
	 * Outputs of the product system with the once referenced by the applied
	 * LCIA method(s) regarding e.g. correct assignment, coverage/gaps,
	 * doublecounting, etc. [Note: See also definition for entry "LCIA
	 * results".]
	 * 
	 */
	@XmlEnumValue("LCIA results calculation")
	LCIA_RESULTS_CALCULATION("LCIA results calculation"),

	/**
	 * Review/verification is done regarding e.g. goal definition, subsequent
	 * scope definition and corresponding product system description,
	 * appropriate identification and definition of function and functional
	 * unit, system boundary and cut-off criteria setting, choice of appropriate
	 * LCI modelling principles and approaches for multi-functional processes.
	 * 
	 */
	@XmlEnumValue("Goal and scope definition")
	GOAL_AND_SCOPE_DEFINITION("Goal and scope definition");
	private final String value;

	ReviewScope(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ReviewScope fromValue(String v) {
		for (ReviewScope c : ReviewScope.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
