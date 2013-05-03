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
import org.openlca.core.model.AdminInfo;
import org.openlca.core.model.ModelingAndValidation;
import org.openlca.core.model.Process;
import org.openlca.core.model.Source;
import org.openlca.core.model.modelprovider.IModelComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches references to a source
 * 
 * @author Sebastian Greve
 * 
 */
public class SourceReferenceSearcher implements IReferenceSearcher<Source> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * The logger
	 */
	private final JobHandler logger = Jobs.getHandler(Jobs.MAIN_JOB_HANDLER);

	@Override
	public Reference[] findReferences(final IDatabase database,
			final Source component) {
		final List<Reference> references = new ArrayList<>();
		try {
			// Search for admin info objects with the given source as
			// publication and create a reference object for each found
			logger.subJob("searching for " + component.getName());
			final Map<String, Object> properties = new HashMap<>();
			properties.put("publication.id", component.getId());
			final List<AdminInfo> adminInfoWithPublication = database
					.selectAll(AdminInfo.class, properties);
			for (final AdminInfo adminInfo : adminInfoWithPublication) {
				final IModelComponent process = database.selectDescriptor(
						Process.class, adminInfo.getId());
				final Reference reference = new Reference(Reference.OPTIONAL,
						"Process", process.getName(), "Publication") {

					@Override
					public void solve() {
						try {
							adminInfo.setPublication(null);
							database.update(adminInfo);
						} catch (final Exception e) {
							log.error("Setting publication to null failed", e);
						}
					}
				};
				references.add(reference);
			}

			// Search for modeling and validations with the given source
			// and create a reference object for each found
			logger.subJob("searching for " + component.getName());
			properties.clear();
			properties.put("source.id", component.getId());
			final List<ModelingAndValidation> mavWithSource = database
					.selectAll(ModelingAndValidation.class, properties);
			for (final ModelingAndValidation modelingAndValidation : mavWithSource) {
				final IModelComponent process = database.selectDescriptor(
						Process.class, modelingAndValidation.getId());
				final Reference reference = new Reference(Reference.OPTIONAL,
						"Process", process.getName(), null) {

					@Override
					public void solve() {
						try {
							modelingAndValidation.remove(component);
							database.update(modelingAndValidation);
						} catch (final Exception e) {
							log.error("Removing component failed", e);
						}
					}
				};
				references.add(reference);
			}
		} catch (final Exception e) {
			log.error("Find references failed", e);
		}
		return references.toArray(new Reference[references.size()]);

	}
}
