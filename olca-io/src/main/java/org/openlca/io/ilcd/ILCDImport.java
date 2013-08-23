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
import java.util.Iterator;

import org.openlca.core.database.IDatabase;
import org.openlca.core.jobs.IProgressMonitor;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.ZipStore;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.MethodBag;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.io.ilcd.input.ContactImport;
import org.openlca.io.ilcd.input.FlowImport;
import org.openlca.io.ilcd.input.FlowPropertyImport;
import org.openlca.io.ilcd.input.MethodImport;
import org.openlca.io.ilcd.input.ProcessImport;
import org.openlca.io.ilcd.input.SourceImport;
import org.openlca.io.ilcd.input.SystemImport;
import org.openlca.io.ilcd.input.UnitGroupImport;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.MapType;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ILCDImport implements Runnable {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private File zip;
	private IProgressMonitor monitor;
	private IDatabase database;
	private boolean importFlows = false;

	public ILCDImport(File zip, IProgressMonitor monitor, IDatabase database) {
		if (zip == null || monitor == null)
			throw new IllegalArgumentException("NULL argument(s) not allowed.");
		this.zip = zip;
		this.monitor = monitor;
		this.database = database;
	}

	public void setImportFlows(boolean importFlows) {
		this.importFlows = importFlows;
	}

	@Override
	public void run() {
		if (monitor.isCanceled())
			return;
		monitor.beginTask("ILCD Import", 100);
		ZipStore zipStore = new ZipStore(zip);
		tryImportContacts(zipStore);
		tryImportSources(zipStore);
		tryImportUnits(zipStore);
		tryImportFlowProperties(zipStore);
		if (importFlows) {
			tryImportFlows(zipStore);
		}
		tryImportProcesses(zipStore);
		tryImportMethods(zipStore);
		tryCloseStore(zipStore);
		monitor.done();
	}

	private void tryCloseStore(ZipStore zipStore) {
		try {
			zipStore.close();
		} catch (Exception e) {
			log.warn("Could not close zip file " + zip, e);
		}
	}

	private void tryImportContacts(ZipStore zipStore) {
		if (monitor.isCanceled())
			return;
		try {
			monitor.subTask("Import contacts");
			importContacts(zipStore);
			monitor.worked(5);
		} catch (Exception e) {
			log.error("Contact import failed", e);
		}
	}

	private void importContacts(ZipStore zipStore) throws Exception {
		Iterator<Contact> it = zipStore.iterator(Contact.class);
		while (it.hasNext() && !monitor.isCanceled()) {
			Contact contact = it.next();
			ContactImport contactImport = new ContactImport(zipStore, database);
			contactImport.run(contact);
		}
	}

	private void tryImportSources(ZipStore zipStore) {
		if (monitor.isCanceled())
			return;
		try {
			monitor.subTask("Import sources");
			importSources(zipStore);
			monitor.worked(10);
		} catch (Exception e) {
			log.error("Source import failed", e);
		}
	}

	private void importSources(ZipStore zipStore) throws Exception {
		Iterator<Source> it = zipStore.iterator(Source.class);
		while (it.hasNext() && !monitor.isCanceled()) {
			Source source = it.next();
			SourceImport sourceImport = new SourceImport(zipStore, database);
			sourceImport.run(source);
		}
	}

	private void tryImportUnits(ZipStore zipStore) {
		if (monitor.isCanceled())
			return;
		try {
			monitor.subTask("Import unit groups");
			importUnitGroups(zipStore);
			monitor.worked(15);
		} catch (Exception e) {
			log.error("Unit group import failed", e);
		}
	}

	private void importUnitGroups(ZipStore zipStore) throws Exception {
		Iterator<UnitGroup> it = zipStore.iterator(UnitGroup.class);
		while (it.hasNext() && !monitor.isCanceled()) {
			UnitGroup group = it.next();
			UnitGroupImport groupImport = new UnitGroupImport(zipStore,
					database);
			groupImport.run(group);
		}
	}

	private void tryImportFlowProperties(ZipStore zipStore) {
		if (monitor.isCanceled())
			return;
		try {
			monitor.subTask("Import flow properties");
			importFlowProperties(zipStore);
			monitor.worked(20);
		} catch (Exception e) {
			log.error("Flow property import failed", e);
		}
	}

	private void importFlowProperties(ZipStore zipStore) throws Exception {
		Iterator<FlowProperty> it = zipStore.iterator(FlowProperty.class);
		while (it.hasNext() && !monitor.isCanceled()) {
			FlowProperty property = it.next();
			FlowPropertyImport propertyImport = new FlowPropertyImport(
					zipStore, database);
			propertyImport.run(property);
		}
	}

	private void tryImportFlows(ZipStore zipStore) {
		if (monitor.isCanceled())
			return;
		try {
			monitor.subTask("Import flows");
			importFlows(zipStore);
			monitor.worked(30);
		} catch (Exception e) {
			log.error("Flow import failed");
		}
	}

	private void importFlows(ZipStore zipStore) throws Exception {
		Iterator<Flow> it = zipStore.iterator(Flow.class);
		while (it.hasNext() && !monitor.isCanceled()) {
			Flow flow = it.next();
			FlowImport flowImport = new FlowImport(zipStore, database);
			flowImport.run(flow);
		}
	}

	private void tryImportProcesses(ZipStore zipStore) {
		if (monitor.isCanceled())
			return;
		try {
			monitor.subTask("Import processes");
			importProcesses(zipStore);
			monitor.worked(70);
		} catch (Exception e) {
			log.error("Process import failed", e);
		}
	}

	private void importProcesses(ZipStore zipStore) throws Exception {
		FlowMap flowMap = new FlowMap(MapType.ILCD_FLOW);
		Iterator<Process> it = zipStore.iterator(Process.class);
		ProcessImport processImport = new ProcessImport(zipStore, database);
		processImport.setFlowMap(flowMap);
		while (it.hasNext() && !monitor.isCanceled()) {
			Process process = it.next();
			ProcessBag bag = new ProcessBag(process);
			if (bag.hasProductModel()) {
				monitor.subTask("Import product system "
						+ Strings.cut(bag.getName(), 50));
				SystemImport systemImport = new SystemImport(zipStore, database);
				systemImport.run(process);
			} else {
				monitor.subTask("Import process "
						+ Strings.cut(bag.getName(), 50));
				processImport.run(process);
			}
		}
	}

	private void tryImportMethods(ZipStore zipStore) {
		if (monitor.isCanceled())
			return;
		try {
			monitor.subTask("Import impact categories");
			importMethods(zipStore);
			monitor.worked(100);
		} catch (Exception e) {
			log.error("Impact category import failed", e);
		}
	}

	private void importMethods(ZipStore zipStore) throws Exception {
		Iterator<LCIAMethod> it = zipStore.iterator(LCIAMethod.class);
		while (it.hasNext() && !monitor.isCanceled()) {
			LCIAMethod method = it.next();
			MethodBag bag = new MethodBag(method);
			monitor.subTask("Import indicator "
					+ Strings.cut(bag.getImpactIndicator(), 50));
			MethodImport methodImport = new MethodImport(zipStore, database);
			methodImport.run(method);
		}
	}

}
