/*******************************************************************************
 * Copyright (c) 2007 - 2012 GreenDeltaTC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Mozilla Public License v1.1
 * which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 *
 * Contributors:
 *     	GreenDeltaTC - initial API and implementation
 *		www.greendeltatc.com
 *		tel.:  +49 30 4849 6030
 *		mail:  gdtc@greendeltatc.com
 *******************************************************************************/

package org.openlca.core.database.internal;

import java.sql.Connection;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.IProductSystemBuilder;
import org.openlca.core.jobs.IProgressMonitor;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.matrix.ProductIndexBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductSystemBuilder implements IProductSystemBuilder {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private IProgressMonitor progressMonitor;

	private MatrixCache matrixCache;
	private IDatabase database;
	private boolean preferSystemProcesses;

	public ProductSystemBuilder(MatrixCache matrixCache,
			boolean preferSystemProcesses) {
		this.matrixCache = matrixCache;
		this.database = matrixCache.getDatabase();
		this.preferSystemProcesses = preferSystemProcesses;
	}

	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	@Override
	public void autoComplete(ProductSystem system) {
		if (system == null || system.getReferenceExchange() == null
				|| system.getReferenceProcess() == null)
			return;
		Process refProcess = system.getReferenceProcess();
		Flow refProduct = system.getReferenceExchange().getFlow();
		if (refProduct == null)
			return;
		LongPair ref = new LongPair(refProcess.getId(), refProduct.getId());
		autoComplete(system, ref);
	}

	@Override
	public void autoComplete(ProductSystem system, LongPair processProduct) {
		try (Connection con = database.createConnection()) {
			log.trace("auto complete product system {}", system);
			run(system, processProduct);
			log.trace("update product system");
			database.createDao(ProductSystem.class).update(system);
		} catch (Exception e) {
			log.error("Failed to auto complete product system " + system, e);
		}
	}

	private void run(ProductSystem system, LongPair processProduct) {
		ProductIndexBuilder builder = new ProductIndexBuilder(matrixCache);
		builder.setPreferredType(preferSystemProcesses ? ProcessType.LCI_RESULT
				: ProcessType.UNIT_PROCESS);
		ProductIndex index = builder.build(processProduct);
		for (LongPair input : index.getLinkedInputs()) {
			LongPair output = index.getLinkedOutput(input);
			if (output == null)
				continue;
			if (!system.getProcesses().contains(output.getFirst()))
				system.getProcesses().add(output.getFirst());
			if (containsLink(system, input, output))
				continue;
			ProcessLink link = new ProcessLink();
			link.setFlowId(input.getSecond());
			link.setProviderId(output.getFirst());
			link.setRecipientId(input.getFirst());
			system.getProcessLinks().add(link);
		}

	}

	private boolean containsLink(ProductSystem system, LongPair input,
			LongPair output) {
		for (ProcessLink link : system.getProcessLinks()) {
			if (link.getFlowId() == input.getSecond()
					&& link.getRecipientId() == input.getFirst()
					&& link.getProviderId() == output.getFirst())
				return true;
		}
		return false;
	}

}
