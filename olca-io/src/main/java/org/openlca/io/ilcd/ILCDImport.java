package org.openlca.io.ilcd;

import java.util.Iterator;

import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
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
import org.openlca.io.ilcd.input.ImportConfig;
import org.openlca.io.ilcd.input.MethodImport;
import org.openlca.io.ilcd.input.ProcessImport;
import org.openlca.io.ilcd.input.SourceImport;
import org.openlca.io.ilcd.input.SystemImport;
import org.openlca.io.ilcd.input.UnitGroupImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class ILCDImport implements FileImport {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private boolean canceled = false;
	private EventBus eventBus;
	private final ImportConfig config;

	public ILCDImport(ImportConfig config) {
		this.config = config;
	}

	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public void cancel() {
		this.canceled = true;
	}

	@Override
	public void run() {
		if (canceled)
			return;
		tryImportContacts();
		tryImportSources();
		tryImportUnits();
		tryImportFlowProperties();
		if (config.importFlows)
			tryImportFlows();
		tryImportProcesses();
		tryImportMethods();
		tryCloseStore();
	}

	private void tryCloseStore() {
		try {
			config.store.close();
		} catch (Exception e) {
			log.warn("Could not close zip file", e);
		}
	}

	private void tryImportContacts() {
		if (canceled)
			return;
		try {
			importContacts();
		} catch (Exception e) {
			log.error("Contact import failed", e);
		}
	}

	private void importContacts() throws Exception {
		Iterator<Contact> it = config.store.iterator(Contact.class);
		while (it.hasNext() && !canceled) {
			Contact contact = it.next();
			ContactImport contactImport = new ContactImport(config);
			contactImport.run(contact);
		}
	}

	private void tryImportSources() {
		if (canceled)
			return;
		try {
			Iterator<Source> it = config.store.iterator(Source.class);
			while (it.hasNext() && !canceled) {
				Source source = it.next();
				fireEvent(new SourceBag(source, config.ilcdConfig)
						.getShortName());
				SourceImport sourceImport = new SourceImport(config);
				sourceImport.run(source);
			}
		} catch (Exception e) {
			log.error("Source import failed", e);
		}
	}

	private void tryImportUnits() {
		if (canceled)
			return;
		try {
			Iterator<UnitGroup> it = config.store.iterator(UnitGroup.class);
			while (it.hasNext() && !canceled) {
				UnitGroup group = it.next();
				fireEvent(new UnitGroupBag(group, config.ilcdConfig).getName());
				UnitGroupImport groupImport = new UnitGroupImport(config);
				groupImport.run(group);
			}
		} catch (Exception e) {
			log.error("Unit group import failed", e);
		}
	}

	private void tryImportFlowProperties() {
		if (canceled)
			return;
		try {
			Iterator<FlowProperty> it = config.store
					.iterator(FlowProperty.class);
			while (it.hasNext() && !canceled) {
				FlowProperty property = it.next();
				fireEvent(new FlowPropertyBag(property, config.ilcdConfig)
						.getName());
				FlowPropertyImport propertyImport = new FlowPropertyImport(
						config);
				propertyImport.run(property);
			}
		} catch (Exception e) {
			log.error("Flow property import failed", e);
		}
	}

	private void tryImportFlows() {
		if (canceled)
			return;
		try {
			Iterator<Flow> it = config.store.iterator(Flow.class);
			while (it.hasNext() && !canceled) {
				Flow flow = it.next();
				fireEvent(new FlowBag(flow, config.ilcdConfig).getName());
				FlowImport flowImport = new FlowImport(config);
				flowImport.run(flow);
			}
		} catch (Exception e) {
			log.error("Flow import failed", e);
		}
	}

	private void tryImportProcesses() {
		if (canceled)
			return;
		try {
			importProcesses();
		} catch (Exception e) {
			log.error("Process import failed", e);
		}
	}

	private void importProcesses() throws Exception {
		Iterator<Process> it = config.store.iterator(Process.class);
		ProcessImport processImport = new ProcessImport(config);
		while (it.hasNext() && !canceled) {
			Process process = it.next();
			ProcessBag bag = new ProcessBag(process, config.ilcdConfig);
			fireEvent(bag.getName());
			if (bag.hasProductModel()) {
				SystemImport systemImport = new SystemImport(config);
				systemImport.run(process);
			} else {
				processImport.run(process);
			}
		}
	}

	private void tryImportMethods() {
		if (canceled)
			return;
		try {
			Iterator<LCIAMethod> it = config.store.iterator(LCIAMethod.class);
			while (it.hasNext() && !canceled) {
				LCIAMethod method = it.next();
				MethodBag bag = new MethodBag(method);
				fireEvent(bag.getImpactIndicator());
				MethodImport methodImport = new MethodImport(config);
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
