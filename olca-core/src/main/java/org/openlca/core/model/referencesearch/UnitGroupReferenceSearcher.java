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
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.modelprovider.IModelComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches references to an unit group
 * 
 * @author Sebastian Greve
 * 
 */
public class UnitGroupReferenceSearcher implements
		IReferenceSearcher<UnitGroup> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * The logger
	 */
	private final JobHandler logger = Jobs.getHandler(Jobs.MAIN_JOB_HANDLER);

	@Override
	public Reference[] findReferences(final IDatabase database,
			final UnitGroup component) {
		final List<Reference> problems = new ArrayList<>();
		try {
			// Search for flow properties with the given unit group and create a
			// problem object for each found
			logger.subJob("searching for " + component.getName());
			final Map<String, Object> properties = new HashMap<>();
			properties.put("unitGroupId", component.getId());
			final IModelComponent[] flowPropertiesWithUnitGroup = database
					.selectDescriptors(FlowProperty.class, properties);
			for (final IModelComponent modelComponent : flowPropertiesWithUnitGroup) {
				final Reference problem = new NeccessaryReference(
						Reference.REQUIRED, "FlowProperty",
						modelComponent.getName(), null);
				problems.add(problem);
			}
		} catch (final Exception e) {
			log.error("Find references failed", e);
		}
		return problems.toArray(new Reference[problems.size()]);
	}

}
