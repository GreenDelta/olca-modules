/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model.modelprovider;

import org.openlca.core.model.Parameter;

/**
 * Interface for parameterized components
 * 
 * @author Sebastian Greve
 * 
 */
public interface IParameterizedComponent extends IModelComponent {

	/**
	 * Adds a parameter to the process
	 * 
	 * @param parameter
	 *            The parameter to be added
	 */
	void add(Parameter parameter);

	/**
	 * Getter of the parameters
	 * 
	 * @return The parameters of the process
	 */
	Parameter[] getParameters();

	/**
	 * Removes a parameter from the process
	 * 
	 * @param parameter
	 *            The parameter to be removed
	 */
	void remove(Parameter parameter);

}
