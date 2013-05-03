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
 * This class represents problems while operating on objects
 * 
 * @author Sebastian Greve
 * 
 */
public abstract class Reference {

	/**
	 * Means that the reference cannot be removed
	 */
	public static final int REQUIRED = 0;

	/**
	 * Means that the reference can be removed
	 */
	public static final int OPTIONAL = 1;

	private String referencedFieldName;
	private String referencedObjectName;
	private String referencedObjectType;
	private int type;

	/**
	 * Creates a new reference object
	 * 
	 * @param type
	 *            The type of the reference. One of: {@link #REQUIRED},
	 *            {@link #OPTIONAL}
	 * @param referencedObjectType
	 *            The type of the referenced object
	 * @param referencedObjectName
	 *            The name of the referenced object
	 * @param referencedFieldName
	 *            The name of the referenced field
	 */
	protected Reference(int type, String referencedObjectType,
			String referencedObjectName, String referencedFieldName) {
		this.type = type;
		this.referencedObjectType = referencedObjectType;
		this.referencedObjectName = referencedObjectName;
		this.referencedFieldName = referencedFieldName;
	}

	/** Get the field name from the object is referenced. */
	public String getReferencedFieldName() {
		return referencedFieldName;
	}

	/** Get the name of the referenced object. */
	public String getReferencedObjectName() {
		return referencedObjectName;
	}

	/** Get the type of the referenced object. */
	public String getReferencedObjectType() {
		return referencedObjectType;
	}

	/** Get the reference type: 0 = NECESSARY | 1 UNNECESSARY. */
	public int getType() {
		return type;
	}

	/** Solve a reference error (mostly by deleting the reference). */
	public abstract void solve();

}
