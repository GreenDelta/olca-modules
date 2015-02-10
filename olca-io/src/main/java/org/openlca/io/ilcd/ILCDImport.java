package org.openlca.io.ilcd;

import java.io.File;
import java.util.Iterator;

import org.openlca.core.database.IDatabase;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.ZipStore;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.FlowBag;
import org.openlca.ilcd.util.FlowPropertyBag;
import org.openlca.ilcd.util.MethodBag;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.ilcd.util.SourceBag;
import org.openlca.ilcd.util.UnitGroupBag;
import org.openlca.io.FileImport;
import org.openlca.io.ImportEvent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class ILCDImport implements FileImport {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private File zip;
	private IDatabase database;
	private boolean importFlows = false;
	private boolean canceled = false;
	private EventBus eventBus;

	public ILCDImport(File zip, IDatabase database) {
		if (zip == null)
			throw new IllegalArgumentException("NULL argument(s) not allowed.");
		this.zip = zip;
		this.database = database;
	}

	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public void cancel() {
		this.canceled = true;
	}

	public void setImportFlows(boolean importFlows) {
		this.importFlows = importFlows;
	}

	@Override
	public void run() {
		if (canceled)
			return;
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
	}

	private void tryCloseStore(ZipStore zipStore) {
		try {
			zipStore.close();
		} catch (Exception e) {
			log.warn("Could not close zip file " + zip, e);
		}
	}

	private void tryImportContacts(ZipStore zipStore) {
		if (canceled)
			return;
		try {
			importContacts(zipStore);
		} catch (Exception e) {
			log.error("Contact import failed", e);
		}
	}

	private void importContacts(ZipStore zipStore) throws Exception {
		Iterator<Contact> it = zipStore.iterator(Contact.class);
		while (it.hasNext() && !canceled) {
			Contact contact = it.next();
			ContactImport contactImport = new ContactImport(zipStore, database);
			contactImport.run(contact);
		}
	}

	private void tryImportSources(ZipStore zipStore) {
		if (canceled)
			return;
		try {
			Iterator<Source> it = zipStore.iterator(Source.class);
			while (it.hasNext() && !canceled) {
				Source source = it.next();
				fireEvent(new SourceBag(source).getShortName());
				SourceImport sourceImport = new SourceImport(zipStore, database);
				sourceImport.run(source);
			}
		} catch (Exception e) {
			log.error("Source import failed", e);
		}
	}

	private void tryImportUnits(ZipStore zipStore) {
		if (canceled)
			return;
		try {
			Iterator<UnitGroup> it = zipStore.iterator(UnitGroup.class);
			while (it.hasNext() && !canceled) {
				UnitGroup group = it.next();
				fireEvent(new UnitGroupBag(group).getName());
				UnitGroupImport groupImport = new UnitGroupImport(zipStore,
						database);
				groupImport.run(group);
			}
		} catch (Exception e) {
			log.error("Unit group import failed", e);
		}
	}

	private void tryImportFlowProperties(ZipStore zipStore) {
		if (canceled)
			return;
		try {
			Iterator<FlowProperty> it = zipStore.iterator(FlowProperty.class);
			while (it.hasNext() && !canceled) {
				FlowProperty property = it.next();
				fireEvent(new FlowPropertyBag(property).getName());
				FlowPropertyImport propertyImport = new FlowPropertyImport(
						zipStore, database);
				propertyImport.run(property);
			}
		} catch (Exception e) {
			log.error("Flow property import failed", e);
		}
	}

	private void tryImportFlows(ZipStore zipStore) {
		if (canceled)
			return;
		try {
			Iterator<Flow> it = zipStore.iterator(Flow.class);
			while (it.hasNext() && !canceled) {
				Flow flow = it.next();
				fireEvent(new FlowBag(flow).getName());
				FlowImport flowImport = new FlowImport(zipStore, database);
				flowImport.run(flow);
			}
		} catch (Exception e) {
			log.error("Flow import failed", e);
		}
	}

	private void tryImportProcesses(ZipStore zipStore) {
		if (canceled)
			return;
		try {
			importProcesses(zipStore);
		} catch (Exception e) {
			log.error("Process import failed", e);
		}
	}

	private void importProcesses(ZipStore zipStore) throws Exception {
		FlowMap flowMap = new FlowMap(MapType.ILCD_FLOW);
		Iterator<Process> it = zipStore.iterator(Process.class);
		ProcessImport processImport = new ProcessImport(zipStore, database);
		processImport.setFlowMap(flowMap);
		while (it.hasNext() && !canceled) {
			Process process = it.next();
			ProcessBag bag = new ProcessBag(process);
			fireEvent(bag.getName());
			if (bag.hasProductModel()) {
				SystemImport systemImport = new SystemImport(zipStore, database);
				systemImport.run(process);
			} else {
				processImport.run(process);
			}
		}
	}

	private void tryImportMethods(ZipStore zipStore) {
		if (canceled)
			return;
		try {
			Iterator<LCIAMethod> it = zipStore.iterator(LCIAMethod.class);
			while (it.hasNext() && !canceled) {
				LCIAMethod method = it.next();
				MethodBag bag = new MethodBag(method);
				fireEvent(bag.getImpactIndicator());
				MethodImport methodImport = new MethodImport(zipStore, database);
				methodImport.run(method);
			}
		} catch (Exception e) {
			log.error("Impact category import failed", e);
		}
	}

	private void fireEvent(String dataSet) {
		log.trace("import data set {}", dataSet);
		if (eventBus == null)
			return;
		eventBus.post(new ImportEvent(dataSet));
	}

}
