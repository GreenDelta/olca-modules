/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/

package org.openlca.io.ilcd;

import java.io.File;

import org.openlca.core.database.DataProviderException;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.modelprovider.IModelComponent;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.io.ZipStore;
import org.openlca.io.ilcd.output.ActorExport;
import org.openlca.io.ilcd.output.FlowExport;
import org.openlca.io.ilcd.output.FlowPropertyExport;
import org.openlca.io.ilcd.output.ProcessExport;
import org.openlca.io.ilcd.output.SourceExport;
import org.openlca.io.ilcd.output.SystemExport;
import org.openlca.io.ilcd.output.UnitGroupExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The entry point for the ILCD export of model components.
 * 
 * @author Michael Srocka
 * 
 */
public class ILCDExport {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private ZipStore ilcdStore;
	int errorNo = 0;

	public ILCDExport(File targetDir) {
		try {
			ilcdStore = new ZipStore(file(targetDir));
		} catch (Exception e) {
			log.error("ILCD export failed", e);
		}
	}

	private File file(File parent) {
		if (!parent.exists())
			parent.mkdirs();
		String name = "ILCD.zip";
		int i = numberOfFiles(parent);
		if (i > 0)
			name = "ILCD_(" + i + ").zip";
		return new File(parent, name);
	}

	private int numberOfFiles(File parent) {
		int i = 0;
		for (String name : parent.list()) {
			if (name.startsWith("ILCD") && name.endsWith(".zip"))
				i++;
		}
		return i;
	}

	public void export(IModelComponent component, IDatabase database) {
		if (component == null || database == null)
			throw new IllegalArgumentException(
					"Component and database cannot be NULL.");
		if (errorNo > 10) // stop exporting after 10 errors
			return;
		try {
			tryExport(component, database);
		} catch (Exception e) {
			errorNo++;
			log.error("Export of component " + component + " failed", e);
		}
	}

	private void tryExport(IModelComponent component, IDatabase database)
			throws DataProviderException, DataStoreException {
		if (component instanceof ProductSystem) {
			ProductSystem system = database.select(ProductSystem.class,
					component.getId());
			SystemExport export = new SystemExport(database, ilcdStore);
			export.run(system);
		}

		if (component instanceof Process) {
			Process process = database.select(Process.class, component.getId());
			ProcessExport export = new ProcessExport(database, ilcdStore);
			export.run(process);

		} else if (component instanceof Flow) {
			Flow flow = database.select(Flow.class, component.getId());
			FlowExport flowExport = new FlowExport(database, ilcdStore);
			flowExport.run(flow);

		} else if (component instanceof FlowProperty) {
			FlowProperty property = database.select(FlowProperty.class,
					component.getId());
			FlowPropertyExport export = new FlowPropertyExport(database,
					ilcdStore);
			export.run(property);

		} else if (component instanceof UnitGroup) {
			UnitGroup unitGroup = database.select(UnitGroup.class,
					component.getId());
			UnitGroupExport export = new UnitGroupExport(database, ilcdStore);
			export.run(unitGroup);

		} else if (component instanceof Actor) {
			Actor actor = database.select(Actor.class, component.getId());
			ActorExport export = new ActorExport(database, ilcdStore);
			export.run(actor);

		} else if (component instanceof Source) {
			Source source = database.select(Source.class, component.getId());
			SourceExport export = new SourceExport(database, ilcdStore);
			export.run(source);
		}
	}

	public void close() {
		if (ilcdStore == null)
			return;
		try {
			ilcdStore.close();
		} catch (Exception e) {
			log.error("Could not close ZipStore", e);
		}
	}

}
