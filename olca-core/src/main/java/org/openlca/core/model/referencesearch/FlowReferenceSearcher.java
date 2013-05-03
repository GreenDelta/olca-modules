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
import org.openlca.core.model.Flow;
import org.openlca.core.model.LCIAMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.modelprovider.IModelComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches references to a flow
 * 
 * @author Sebastian Greve
 * 
 */
public class FlowReferenceSearcher implements IReferenceSearcher<Flow> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * The logger
	 */
	private final JobHandler logger = Jobs.getHandler(Jobs.MAIN_JOB_HANDLER);

	@Override
	public Reference[] findReferences(final IDatabase database,
			final Flow component) {

		final List<Reference> references = new ArrayList<>();
		try {
			// search for processes with the given flow
			// as exchange and create a reference object for each found
			logger.subJob("searching for " + component.getName());

			final Map<String, Object> properties = new HashMap<>();
			properties.put("exchanges.flow.id", component.getId());

			final IModelComponent[] processesWithExchangeWithFlow = database
					.selectDescriptors(Process.class, properties);
			for (final IModelComponent modelComponent : processesWithExchangeWithFlow) {
				final Reference reference = new NeccessaryReference(
						Reference.REQUIRED, "Process",
						modelComponent.getName(), null);
				references.add(reference);
			}

			// search for LCIA methods with the given flow
			// as factor and create a reference object for each found
			logger.subJob("searching for " + component.getName());
			properties.clear();
			properties.put("lciaCategories.lciaFactors.flow.id",
					component.getId());
			final IModelComponent[] lciaMethodsWithLCIACategoriesWithLCIAFactorWithFlow = database
					.selectDescriptors(LCIAMethod.class, properties);
			for (final IModelComponent modelComponent : lciaMethodsWithLCIACategoriesWithLCIAFactorWithFlow) {
				final Reference reference = new NeccessaryReference(
						Reference.REQUIRED, "LCIAMethod",
						modelComponent.getName(), null);
				references.add(reference);
			}
		} catch (final Exception e) {
			log.error("Find references failed", e);
		}
		return references.toArray(new Reference[references.size()]);
	}
}
