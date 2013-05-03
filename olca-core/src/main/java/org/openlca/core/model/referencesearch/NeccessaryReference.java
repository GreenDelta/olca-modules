/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model.referencesearch;

/**
 * An unsolvable problem is a problem that cannot be solved (Mostly an error,
 * warnings should be solvable)
 * 
 * @author Sebastian Greve
 * 
 */
public class NeccessaryReference extends Reference {

	/**
	 * @param type
	 *            The type of reference
	 * @param referencedObjectType
	 *            The referenced objects type
	 * @param referencedObjectName
	 *            The referenced objects name
	 * @param referencedFieldName
	 *            The referenced objects field name
	 */
	public NeccessaryReference(final int type,
			final String referencedObjectType,
			final String referencedObjectName, final String referencedFieldName) {
		super(type, referencedObjectType, referencedObjectName,
				referencedFieldName);
	}

	@Override
	public final void solve() {
		// does nothing, because problem is unsolvable
	}

}
