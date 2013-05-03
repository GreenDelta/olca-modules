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

import org.openlca.core.database.DataProviderException;
import org.openlca.core.database.IDatabase;
import org.openlca.core.jobs.JobHandler;
import org.openlca.core.jobs.Jobs;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.LCIAMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.modelprovider.IModelComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches references to a flow
 * 
 * @author Sebastian Greve
 * 
 */
public class FlowPropertyReferenceFinder implements
		IReferenceSearcher<FlowProperty> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private final JobHandler jobHandler = Jobs
			.getHandler(Jobs.MAIN_JOB_HANDLER);

	@Override
	public Reference[] findReferences(IDatabase database,
			FlowProperty flowProperty) {
		List<Reference> references = new ArrayList<>();
		try {
			findFlowReferences(database, flowProperty, references);
			findExchangeReferences(database, flowProperty, references);
			findImpactMethodReferences(database, flowProperty, references);
			findUnitGroupReferences(database, flowProperty, references);
		} catch (Exception e) {
			log.error("Find references failed", e);
		}
		return references.toArray(new Reference[references.size()]);
	}

	private void findUnitGroupReferences(final IDatabase database,
			final FlowProperty flowProperty, List<Reference> references)
			throws DataProviderException {
		jobHandler.subJob("Search references: unit groups");
		Map<String, Object> properties = new HashMap<>();
		properties.put("defaultflowProperty.id", flowProperty.getId());
		IModelComponent[] unitGroup = database.selectDescriptors(
				UnitGroup.class, properties);
		for (final IModelComponent modelComponent : unitGroup) {
			Reference reference = new Reference(Reference.OPTIONAL,
					"UnitGroup", modelComponent.getName(),
					"DefaultFlowProperty") {

				@Override
				public void solve() {
					try {
						final UnitGroup u = database.select(UnitGroup.class,
								modelComponent.getId());
						u.setDefaultFlowProperty(null);
						database.update(u);
					} catch (final Exception e) {
						log.error("Changing unit group failed", e);
					}
				}

			};
			references.add(reference);
		}
	}

	private Map<String, Object> findImpactMethodReferences(
			final IDatabase database, final FlowProperty flowProperty,
			List<Reference> references) throws DataProviderException {
		jobHandler.subJob("Search references: impact methods");
		Map<String, Object> properties = new HashMap<>();
		properties.put("lciaCategories.factors.flowProperty.id",
				flowProperty.getId());
		final IModelComponent[] lciaMethodsWithLCIACategoriesWithLCIAFactorWithFlowProperty = database
				.selectDescriptors(LCIAMethod.class, properties);
		for (final IModelComponent method : lciaMethodsWithLCIACategoriesWithLCIAFactorWithFlowProperty) {
			final Reference reference = new NeccessaryReference(
					Reference.REQUIRED, "LCIAMethod", method.getName(), null);
			references.add(reference);
		}
		return properties;
	}

	private void findExchangeReferences(final IDatabase database,
			final FlowProperty flowProperty, List<Reference> references)
			throws DataProviderException {
		jobHandler.subJob("Search references: exchanges");
		Map<String, Object> properties = new HashMap<>();
		properties.put("exchanges.flowProperty.id", flowProperty.getId());
		final IModelComponent[] processesWithExchangeWithFlowProperty = database
				.selectDescriptors(Process.class, properties);
		for (final IModelComponent process : processesWithExchangeWithFlowProperty) {
			final Reference reference = new NeccessaryReference(
					Reference.REQUIRED, "Process", process.getName(), null);
			references.add(reference);
		}
	}

	private void findFlowReferences(final IDatabase database,
			final FlowProperty flowProperty, List<Reference> references)
			throws DataProviderException {
		jobHandler.subJob("Search references: flwos");
		Map<String, Object> properties = new HashMap<>();
		properties.put("flowPropertyFactors.flowProperty.id",
				flowProperty.getId());
		final List<Flow> flowInfows = database
				.selectAll(Flow.class, properties);
		for (final Flow flowInformation : flowInfows) {
			final Flow flow = database.select(Flow.class,
					flowInformation.getId());
			final int type = flowInformation.getReferenceFlowProperty().getId()
					.equals(flowProperty.getId()) ? Reference.REQUIRED
					: Reference.OPTIONAL;
			final Reference reference = new Reference(
					type,
					"Flow",
					flow.getName(),
					flowInformation.getReferenceFlowProperty().getId()
							.equals(flowProperty.getId()) ? "ReferenceFlowProperty"
							: null) {

				@Override
				public void solve() {
					if (!flowInformation.getReferenceFlowProperty().getId()
							.equals(flowProperty.getId())) {
						boolean found = false;
						int i = 0;
						while (!found
								&& i < flowInformation.getFlowPropertyFactors().length) {
							if (flowInformation.getFlowPropertyFactors()[i]
									.getFlowProperty().getId()
									.equals(flowProperty.getId())) {
								flowInformation.remove(flowInformation
										.getFlowPropertyFactors()[i]);
								found = true;
							} else {
								i++;
							}
						}
						try {
							database.update(flowInformation);
						} catch (final DataProviderException e) {
							log.error("Updating flow information failed", e);
						}
					}
				}
			};
			references.add(reference);
		}
	}
}
