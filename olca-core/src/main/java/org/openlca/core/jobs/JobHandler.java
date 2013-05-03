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

import java.util.ArrayList;
import java.util.List;

/**
 * The job handler is a non-UI progress monitor
 * 
 * @author Sebastian Greve
 * 
 */
// TODO: remove this from the core model, replace this in the UI with
// IProgressMonitor from Eclipse.
public class JobHandler {

	private String id;
	private List<JobListener> listener = new ArrayList<>();

	protected JobHandler(String id) {
		this.id = id;
	}

	protected String getId() {
		return id;
	}

	public void addJobListener(final JobListener listener) {
		this.listener.add(listener);
	}

	public void done() {
		for (final JobListener listener : this.listener) {
			listener.done();
		}
	}

	public boolean jobIsCanceled() {
		boolean isCanceled = false;
		for (final JobListener listener : this.listener) {
			if (listener.isCanceled()) {
				isCanceled = true;
				break;
			}
		}
		return isCanceled;
	}

	public void removeJobListener(final JobListener listener) {
		this.listener.remove(listener);
	}

	public void startJob(final String name, final int length) {
		for (final JobListener listener : this.listener) {
			listener.jobStarted(name, length);
		}
	}

	public void subJob(final String name) {
		for (final JobListener listener : this.listener) {
			listener.subJob(name);
		}
	}

	public void worked(final int amount) {
		for (final JobListener listener : this.listener) {
			listener.worked(amount);
		}
	}
}
