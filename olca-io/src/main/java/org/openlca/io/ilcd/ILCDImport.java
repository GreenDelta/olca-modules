package org.openlca.io.ilcd;

import java.util.Iterator;

import org.openlca.core.database.FlowDao;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.io.ilcd.input.ContactImport;
import org.openlca.io.ilcd.input.FlowImport;
import org.openlca.io.ilcd.input.FlowPropertyImport;
import org.openlca.io.ilcd.input.ImportConfig;
import org.openlca.io.ilcd.input.MethodImport;
import org.openlca.io.ilcd.input.ProcessImport;
import org.openlca.io.ilcd.input.ProviderLinker;
import org.openlca.io.ilcd.input.SourceImport;
import org.openlca.io.ilcd.input.UnitGroupImport;
import org.openlca.io.ilcd.input.models.ModelImport;
import org.openlca.io.maps.FlowMapEntry;

public class ILCDImport implements Runnable {

	private boolean canceled = false;
	private final ImportConfig config;

	public ILCDImport(ImportConfig config) {
		this.config = config;
	}

	public void cancel() {
		this.canceled = true;
	}

	@Override
	public void run() {
		if (canceled)
			return;
		importAll(Contact.class);
		importAll(Source.class);
		importAll(UnitGroup.class);
		importAll(FlowProperty.class);
		if (config.withAllFlows()) {
			importAll(Flow.class);
		}
		tryImportProcesses();
		tryImportMethods();
		tryImportModels();
	}

	private boolean isMapped(Flow flow) {
		if (flow == null)
			return false;
		String uuid = flow.getUUID();
		FlowMapEntry me = config.flowMap().getEntry(uuid);
		if (me == null)
			return false;
		FlowDao dao = new FlowDao(config.db);
		// TODO: we should cache the flow for later but
		// we cannot do this currently: see ExchangeFlow
		return dao.getForRefId(me.targetFlowID()) != null;
	}

	private void tryImportProcesses() {
		if (canceled)
			return;
		try {
			Iterator<Process> it = config.store.iterator(Process.class);
			ProviderLinker linker = new ProviderLinker();
			ProcessImport imp = new ProcessImport(config, linker);
			while (it.hasNext() && !canceled) {
				Process p = it.next();
				if (p == null)
					continue;
				fireEvent(p);
				imp.run(p);
			}
			linker.createLinks(config.db);
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

	private <T extends IDataSet> void importAll(Class<T> type) {
		if (canceled)
			return;
		try {
			var it = config.store().iterator(type);
			while (!canceled && it.hasNext()) {
				importOf(it.next());
			}
		} catch (Exception e) {
			config.log().error("Import of data of type "
				+ type.getSimpleName() + " failed", e);
		}
	}

	private <T extends IDataSet> void importOf(T dataSet) {
		if (dataSet == null)
			return;
		try {
			if (dataSet instanceof Contact contact) {
				new ContactImport(config).run(contact);
			} else if (dataSet instanceof Source source) {
				new SourceImport(config).run(source);
			} else if (dataSet instanceof UnitGroup group) {
				new UnitGroupImport(config).run(group);
			} else if (dataSet instanceof FlowProperty prop) {
				new FlowPropertyImport(config).run(prop);
			} else if (dataSet instanceof Flow flow) {
				new FlowImport(config).run(flow);
			}
		} catch (Exception e) {
			config.log().error("Import of " + dataSet + " failed", e);
		}
	}

}
