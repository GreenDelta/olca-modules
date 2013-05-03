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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.jobs.JobHandler;
import org.openlca.core.jobs.Jobs;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.modelprovider.IModelComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches references to a process
 * 
 * @author Sebastian Greve
 * 
 */
public class ProcessReferenceSearcher implements IReferenceSearcher<Process> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * The logger
	 */
	private final JobHandler logger = Jobs.getHandler(Jobs.MAIN_JOB_HANDLER);

	@Override
	public Reference[] findReferences(final IDatabase database,
			final Process component) {
		final List<Reference> references = new ArrayList<>();
		try {
			// search for product systems containing the given process
			logger.subJob("searching for " + component.getName());
			final Map<String, Object> properties = new HashMap<>();
			properties.put("processes.id", component.getId());
			final IModelComponent[] productSystemWithProcess = database
					.selectDescriptors(ProductSystem.class, properties);
			for (final IModelComponent modelComponent : productSystemWithProcess) {
				final Reference reference = new NeccessaryReference(
						Reference.REQUIRED, "ProductSystem",
						modelComponent.getName(), null);
				references.add(reference);
			}
		} catch (final Exception e) {
			log.error("Find references failed", e);
		}

		return references.toArray(new Reference[references.size()]);
	}
}