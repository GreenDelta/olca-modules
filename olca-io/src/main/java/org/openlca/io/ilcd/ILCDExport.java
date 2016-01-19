package org.openlca.io.ilcd;

import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.ilcd.output.ActorExport;
import org.openlca.io.ilcd.output.ExportConfig;
import org.openlca.io.ilcd.output.FlowExport;
import org.openlca.io.ilcd.output.FlowPropertyExport;
import org.openlca.io.ilcd.output.ImpactMethodExport;
import org.openlca.io.ilcd.output.ProcessExport;
import org.openlca.io.ilcd.output.SourceExport;
import org.openlca.io.ilcd.output.SystemExport;
import org.openlca.io.ilcd.output.UnitGroupExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The entry point for the ILCD export of model components.
 */
public class ILCDExport {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private final ExportConfig config;
	int errorNo = 0;

	public ILCDExport(ExportConfig config) {
		this.config = config;
	}

	public void export(CategorizedEntity component) {
		if (component == null || config.db == null)
			throw new IllegalArgumentException(
					"Component and database cannot be NULL.");
		if (errorNo > 10) // stop exporting after 10 errors
			return;
		try {
			tryExport(component);
		} catch (Exception e) {
			errorNo++;
			log.error("Export of component " + component + " failed", e);
		}
	}

	private void tryExport(CategorizedEntity component) throws Exception {
		if (component instanceof ImpactMethod) {
			ImpactMethodExport export = new ImpactMethodExport(config);
			export.run((ImpactMethod) component);
		} else if (component instanceof ProductSystem) {
			SystemExport export = new SystemExport(config);
			export.run((ProductSystem) component);
		} else if (component instanceof Process) {
			ProcessExport export = new ProcessExport(config);
			export.run((Process) component);
		} else if (component instanceof Flow) {
			FlowExport flowExport = new FlowExport(config);
			flowExport.run((Flow) component);
		} else if (component instanceof FlowProperty) {
			FlowPropertyExport export = new FlowPropertyExport(config);
			export.run((FlowProperty) component);
		} else if (component instanceof UnitGroup) {
			UnitGroupExport export = new UnitGroupExport(config);
			export.run((UnitGroup) component);
		} else if (component instanceof Actor) {
			ActorExport export = new ActorExport(config);
			export.run((Actor) component);
		} else if (component instanceof Source) {
			SourceExport export = new SourceExport(config);
			export.run((Source) component);
		}
	}

	public void close() {
		if (config.store == null)
			return;
		try {
			config.store.close();
		} catch (Exception e) {
			log.error("Could not close store", e);
		}
	}
}
