/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.results.InventoryResult;

/**
 * Interface for calculation the life cycle inventory
 * 
 * @author Sebastian Greve
 * 
 */
public interface ILCICalculator {

	/**
	 * Calculates the LCI result of the given product system,
	 */
	InventoryResult calculate(ProductSystem productSystem, IDatabase database);

}
