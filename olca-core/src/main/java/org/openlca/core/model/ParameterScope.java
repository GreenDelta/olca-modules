package org.openlca.core.model;

/**
 * Parameters can be defined globally, in processes, or LCIA categories. They
 * can be redefined in calculation setups on the project and product system
 * level, but the initial definition is always only global, in processes, or
 * LCIA category.
 */
public enum ParameterScope {

	PROCESS,

	IMPACT,

	GLOBAL

}
