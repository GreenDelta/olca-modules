package org.openlca.ilcd.epd.model.qmeta;

/**
 * The question types as defined in the Q metadata protocol.
 */
public enum QQuestionType {

	/**
	 * Allows to select a single answer from a group of questions.
	 */
	ONE_IN_LIST,

	/**
	 * Allows to answer a question in a group with `yes` or `no`. Multiple
	 * answers can be selected in this group.
	 */
	BOOLEAN

}
