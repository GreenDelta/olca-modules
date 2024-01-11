package org.openlca.io.ilcd.input;

import org.openlca.core.io.ImportLog;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.Processes;
import org.openlca.io.ilcd.input.models.ModelImport;

public class Import implements org.openlca.io.Import {

	private volatile boolean canceled = false;
	private final ImportConfig config;

	public Import(ImportConfig config) {
		this.config = config;
	}

	@Override
	public void cancel() {
		this.canceled = true;
		config.log().info("cancel import");
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public ImportLog log() {
		return config.log();
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
		importAll(Process.class);
		importAll(ImpactMethod.class);
		importAll(Model.class);
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
			} else if (dataSet instanceof Process process) {
				var method = Processes.getInventoryMethod(process);
				if (method != null && method.processType == ProcessType.EPD) {
					new EpdImport(config, process).run();
				} else {
					new ProcessImport(config).run(process);
				}
			} else if (dataSet instanceof ImpactMethod impact) {
				new ImpactImport(config, impact).run();
			} else if (dataSet instanceof Model model) {
				new ModelImport(config).run(model);
			} else {
				config.log().error("No matching import for data set "
					+ dataSet + " available");
			}
		} catch (Exception e) {
			config.log().error("Import of " + dataSet + " failed", e);
		}
	}

}
