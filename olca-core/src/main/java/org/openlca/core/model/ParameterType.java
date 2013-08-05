/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model;


/**
 * Enumeration of parameter types
 */
public enum ParameterType {

	/**
	 * Process parameter
	 */
	PROCESS(0),

	/**
	 * Product system parameter
	 */
	PRODUCT_SYSTEM(1),

	/**
	 * Database parameter
	 */
	DATABASE(2),

	/**
	 * Unspecified parameter
	 */
	UNSPECIFIED(-1);

	/**
	 * The hierarchies level of the type
	 */
	private Integer level;

	/**
	 * Creates a new parameter type
	 * 
	 * @param level
	 *            The hierarchies level of the type
	 */
	private ParameterType(final Integer level) {
		this.level = level;
	}

	/**
	 * Returns the type of a specific class
	 * 
	 * @param clazz
	 *            An implementation of {@link IParameterisable}
	 * @return The type of a specific class
	 */
	public static ParameterType getTypeFor(
			final Class<? extends IParameterisable> clazz) {
		ParameterType type = null;
		if (clazz == Process.class) {
			type = PROCESS;
		} else if (clazz == ProductSystem.class) {
			type = PRODUCT_SYSTEM;
		}
		return type;
	}

	/**
	 * Returns the hierarchies level of the type
	 * 
	 * @return The hierarchies level of the type
	 */
	public Integer getLevel() {
		return level;
	}

}
