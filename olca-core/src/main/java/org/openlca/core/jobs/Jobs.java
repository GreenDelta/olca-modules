/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.jobs;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for job handlers
 */
public class Jobs {

	/**
	 * Map of registered handlers
	 */
	private static Map<String, JobHandler> handler = new HashMap<>();

	/**
	 * The main openLCA job handler
	 */
	public static final String MAIN_JOB_HANDLER = "org.openlca.core.logging";

	/**
	 * Returns the job handler with the given id, if no job handler is
	 * registered, one will be created and registered
	 * 
	 * @param id
	 *            The id of the requested job handler
	 * @return The registered job handler or a new one if no one was registered
	 *         already
	 */
	public static JobHandler getHandler(final String id) {
		JobHandler handler = Jobs.handler.get(id);
		if (handler == null) {
			handler = new JobHandler(id);
			Jobs.handler.put(id, handler);
		}
		return handler;
	}

}
