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

/**
 * Interface for listening to a job handler
 */
public interface JobListener {

	/**
	 * A job is done
	 */
	void done();

	/**
	 * Returns if the job was canceled by the user
	 * 
	 * @return True if the job was canceled, false otherwise
	 */
	boolean isCanceled();

	/**
	 * A job has started
	 * 
	 * @param name
	 *            The name of the job
	 * @param length
	 *            The length of the job
	 */
	void jobStarted(String name, int length);

	/**
	 * A sub job has started
	 * 
	 * @param name
	 *            The name of the sub job
	 */
	void subJob(String name);

	/**
	 * The given amount of jobs are done
	 * 
	 * @param amount
	 *            The worked amount
	 */
	void worked(int amount);

}
