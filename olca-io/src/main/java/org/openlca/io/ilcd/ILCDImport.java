package org.openlca.io.ilcd;

import java.util.Iterator;

import org.openlca.core.database.FlowDao;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.io.FileImport;
import org.openlca.io.ImportEvent;
import org.openlca.io.ilcd.input.ContactImport;
import org.openlca.io.ilcd.input.FlowImport;
import org.openlca.io.ilcd.input.FlowPropertyImport;
import org.openlca.io.ilcd.input.ImportConfig;
import org.openlca.io.ilcd.input.MethodImport;
import org.openlca.io.ilcd.input.ProcessImport;
import org.openlca.io.ilcd.input.SourceImport;
import org.openlca.io.ilcd.input.UnitGroupImport;
import org.openlca.io.ilcd.input.models.ModelImport;
import org.openlca.io.maps.FlowMapEntry;
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
		tryImportModels();
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
			Iterator<Contact> it = config.store.iterator(Contact.class);
			while (it.hasNext() && !canceled) {
				Contact contact = it.next();
				if (contact == null)
					continue;
				ContactImport contactImport = new ContactImport(config);
				contactImport.run(contact);
			}
		} catch (Exception e) {
			log.error("Contact import failed", e);
		}
	}

	private void tryImportSources() {
		if (canceled)
			return;
		try {
			Iterator<Source> it = config.store.iterator(Source.class);
			while (it.hasNext() && !canceled) {
				Source source = it.next();
				if (source == null)
					continue;
				fireEvent(source);
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
				if (group == null)
					continue;
				fireEvent(group);
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
				if (property == null)
					continue;
				fireEvent(property);
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
				if (flow == null || isMapped(flow))
					continue;
				FlowImport flowImport = new FlowImport(config);
				flowImport.run(flow);
			}
		} catch (Exception e) {
			log.error("Flow import failed", e);
		}
	}

	private boolean isMapped(Flow flow) {
		if (flow == null)
			return false;
		String uuid = flow.getUUID();
		FlowMapEntry me = config.getFlowMap().getEntry(uuid);
		if (me == null)
			return false;
		FlowDao dao = new FlowDao(config.db);
		// TODO: we should cache the flow for later but
		// we cannot do this currently: see ExchangeFlow
		return dao.getForRefId(me.referenceFlowID) != null;
	}

	public void importProcess(String id) throws Exception {
		Process p = config.store.get(Process.class, id);
		if (p == null)
			throw new Exception("A process uuid=" + id
					+ " could not be found");
		ProcessImport imp = new ProcessImport(config);
		fireEvent(p);
		imp.run(p);
	}

	private void tryImportProcesses() {
		if (canceled)
			return;
		try {
			Iterator<Process> it = config.store.iterator(Process.class);
			ProcessImport imp = new ProcessImport(config);
			while (it.hasNext() && !canceled) {
				Process p = it.next();
				if (p == null)
					continue;
				fireEvent(p);
				imp.run(p);
			}
		} catch (Exception e) {
			log.error("Process import failed", e);
		}
	}

	private void tryImportMethods() {
		if (canceled)
			return;
		try {
			Iterator<LCIAMethod> it = config.store.iterator(LCIAMethod.class);
			while (it.hasNext() && !canceled) {
				LCIAMethod method = it.next();
				if (method == null)
					continue;
				fireEvent(method);
				MethodImport methodImport = new MethodImport(config);
				methodImport.run(method);
			}
		} catch (Exception e) {
			log.error("Impact category import failed", e);
		}
	}

	private void tryImportModels() {
		if (canceled)
			return;
		try {
			Iterator<Model> it = config.store.iterator(Model.class);
			while (it.hasNext() && !canceled) {
				Model m = it.next();
				if (m == null)
					continue;
				fireEvent(m);
				ModelImport si = new ModelImport(config);
				si.run(m);
			}
		} catch (Exception e) {
			log.error("Product model import failed", e);
		}
	}

	private void fireEvent(IDataSet ds) {
		if (ds == null)
			return;
		String name = LangString.getFirst(ds.getName(), config.langs);
		String info = ds.getDataSetType().toString() + " " + name + " "
				+ ds.getUUID();
		log.trace("import {}", info);
		if (eventBus == null)
			return;
		eventBus.post(new ImportEvent(info));
	}

}
