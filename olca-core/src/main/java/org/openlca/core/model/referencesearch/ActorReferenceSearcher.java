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
import org.openlca.core.model.Actor;
import org.openlca.core.model.AdminInfo;
import org.openlca.core.model.ModelingAndValidation;
import org.openlca.core.model.Process;
import org.openlca.core.model.Project;
import org.openlca.core.model.modelprovider.IModelComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches references to an actor
 * 
 * @author Sebastian Greve
 * 
 */
public class ActorReferenceSearcher implements IReferenceSearcher<Actor> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * The logger
	 */
	private final JobHandler logger = Jobs.getHandler(Jobs.MAIN_JOB_HANDLER);

	@Override
	public Reference[] findReferences(final IDatabase database,
			final Actor component) {
		final List<Reference> references = new ArrayList<>();
		try {
			// search for admin info objects with the given actor as data set
			// owner and create a reference object for each found
			logger.subJob("searching for " + component.getName());
			final Map<String, Object> properties = new HashMap<>();
			properties.put("dataSetOwner.id", component.getId());
			final List<AdminInfo> adminInfoWithDataSetOwner = database
					.selectAll(AdminInfo.class, properties);
			for (final AdminInfo adminInfo : adminInfoWithDataSetOwner) {
				final IModelComponent process = database.selectDescriptor(
						Process.class, adminInfo.getId());
				final Reference reference = new Reference(Reference.OPTIONAL,
						"Process", process.getName(), "DataSetOwner") {

					@Override
					public void solve() {
						try {
							adminInfo.setDataSetOwner(null);
							database.update(adminInfo);
						} catch (final Exception e) {
							log.error("Solve failed", e);
						}
					}
				};
				references.add(reference);
			}

			// search for admin info objects with the given actor as data
			// generator and create a reference object for each found
			logger.subJob("searching for " + component.getName());
			properties.clear();
			properties.put("dataGenerator.id", component.getId());
			final List<AdminInfo> adminInfoWithDataGenerator = database
					.selectAll(AdminInfo.class, properties);
			for (final AdminInfo adminInfo : adminInfoWithDataGenerator) {
				final IModelComponent process = database.selectDescriptor(
						Process.class, adminInfo.getId());
				final Reference reference = new Reference(Reference.OPTIONAL,
						"Process", process.getName(), "DataGenerator") {

					@Override
					public void solve() {
						try {
							adminInfo.setDataGenerator(null);
							database.update(adminInfo);
						} catch (final Exception e) {
							log.error("Solve failed", e);
						}
					}
				};
				references.add(reference);
			}

			// search for admin info objects with the given actor as data
			// documentor and create a reference object for each found
			logger.subJob("searching for " + component.getName());
			properties.clear();
			properties.put("dataDocumentor.id", component.getId());
			final List<AdminInfo> adminInfoWithDataDocumentor = database
					.selectAll(AdminInfo.class, properties);
			for (final AdminInfo adminInfo : adminInfoWithDataDocumentor) {
				final IModelComponent process = database.selectDescriptor(
						Process.class, adminInfo.getId());
				final Reference reference = new Reference(Reference.OPTIONAL,
						"Process", process.getName(), "DataDocumentor") {

					@Override
					public void solve() {
						try {
							adminInfo.setDataDocumentor(null);
							database.update(adminInfo);
						} catch (final Exception e) {
							log.error("Solve failed", e);
						}

					}
				};
				references.add(reference);
			}

			// search for modeling and validation objects with the given actor
			// as reviewer and create a reference object for each found
			logger.subJob("searching for " + component.getName());
			properties.clear();
			properties.put("reviewer.id", component.getId());
			final List<ModelingAndValidation> mavWithReviewer = database
					.selectAll(ModelingAndValidation.class, properties);
			for (final ModelingAndValidation modelingAndValidation : mavWithReviewer) {
				final IModelComponent process = database.selectDescriptor(
						Process.class, modelingAndValidation.getId());
				final Reference reference = new Reference(Reference.OPTIONAL,
						"Process", process.getName(), "Reviewer") {

					@Override
					public void solve() {
						try {
							modelingAndValidation.setReviewer(null);
							database.update(modelingAndValidation);
						} catch (final Exception e) {
							log.error("Solve failed", e);
						}
					}
				};
				references.add(reference);
			}

			// search for projects with the given actor
			// as author and create a reference object for each found
			logger.subJob("searching for " + component.getName());
			properties.clear();
			properties.put("author.id", component.getId());
			final IModelComponent[] projectsWithAuthor = database
					.selectDescriptors(Project.class, properties);
			for (final IModelComponent project : projectsWithAuthor) {
				final Reference reference = new Reference(Reference.OPTIONAL,
						"Project", project.getName(), "Author") {

					@Override
					public void solve() {
						try {
							final Project p = database.select(Project.class,
									project.getId());
							p.setAuthor(null);
							database.update(p);
						} catch (final Exception e) {
							log.error("Solve failed", e);
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
