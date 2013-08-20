package org.openlca.core.model;

/**
 * Parameters can be defined globally and on the process level. They can be
 * redefined in calculation setups on the project and product system level, but
 * the initial definition is always either on the global level or process level.
 */
public enum ParameterScope {

	PROCESS,

	GLOBAL

}
