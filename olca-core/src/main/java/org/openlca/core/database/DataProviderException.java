/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Mozilla Public License v1.1
 * which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 *
 * Contributors:
 *     	GreenDeltaTC - initial API and implementation
 *		www.greendeltatc.com
 *		tel.:  +49 30 4849 6030
 *		mail:  gdtc@greendeltatc.com
 *******************************************************************************/

package org.openlca.core.database;

/**
 * Exception which can be thrown by data operations.
 */
public class DataProviderException extends Exception {

	/**
	 * Serial version id
	 */
	private static final long serialVersionUID = -5756287676019886770L;

	/**
	 * Creates a new instance
	 * 
	 * @param message
	 *            The message of the exception
	 */
	public DataProviderException(final String message) {
		super(message);
	}

	/**
	 * Creates a new instance
	 * 
	 * @param message
	 *            The message of the exception
	 * @param throwable
	 *            The cause of the exception
	 */
	public DataProviderException(final String message, final Throwable throwable) {
		super(message, throwable);
	}

	/**
	 * Creates a new instance
	 * 
	 * @param throwable
	 *            The cause of the exception
	 */
	public DataProviderException(final Throwable throwable) {
		super(throwable);
	}

}
