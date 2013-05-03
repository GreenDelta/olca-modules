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
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.jobs.JobHandler;
import org.openlca.core.jobs.Jobs;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches references to a product system
 * 
 * @author Sebastian Greve
 * 
 */
public class ProductSystemReferenceSearcher implements
		IReferenceSearcher<ProductSystem> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * The logger
	 */
	private final JobHandler logger = Jobs.getHandler(Jobs.MAIN_JOB_HANDLER);

	@Override
	public Reference[] findReferences(final IDatabase database,
			final ProductSystem component) {
		final List<Reference> references = new ArrayList<>();
		try {
			// search for projects containing the given product system
			logger.subJob("searching for " + component.getName());
			final List<Project> projects = database.createDao(Project.class)
					.getAll();
			for (final Project project : projects) {
				if (project.containsProductSystem(component.getId())) {
					final Reference reference = new Reference(
							Reference.OPTIONAL, "Project", project.getName(),
							null) {

						@Override
						public void solve() {
							try {
								project.removeProductSystem(component.getId());
								database.update(project);
							} catch (final Exception e) {
								log.error("Removing component failed", e);
							}
						}
					};
					references.add(reference);
				}
			}
		} catch (final Exception e) {
			log.error("Find references failed", e);
		}
		return references.toArray(new Reference[references.size()]);
	}
}
