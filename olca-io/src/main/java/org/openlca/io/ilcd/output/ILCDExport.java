package org.openlca.io.ilcd.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.io.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * The entry point for the ILCD export of model components.
 */
public class ILCDExport {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	final IDatabase db;
	final DataStore store;
	String lang = "en";

	public ILCDExport(IDatabase db, DataStore store) {
		this.db = db;
		this.store = store;
	}

	public void write(RootEntity component) {
		if (component == null)
			return;
		try {
			tryExport(component);
		} catch (Exception e) {
			log.error("Export of component " + component + " failed", e);
		}
	}

	private void tryExport(RootEntity component) {
		if (component instanceof ImpactMethod) {
			ImpactMethodExport export = new ImpactMethodExport(this);
			export.run((ImpactMethod) component);
		} else if (component instanceof ProductSystem) {
			SystemExport export = new SystemExport(this);
			export.run((ProductSystem) component);
		} else if (component instanceof Process) {
			ProcessExport export = new ProcessExport(this);
			export.run((Process) component);
		} else if (component instanceof Flow) {
			FlowExport flowExport = new FlowExport(this);
			flowExport.run((Flow) component);
		} else if (component instanceof FlowProperty) {
			FlowPropertyExport export = new FlowPropertyExport(this);
			export.run((FlowProperty) component);
		} else if (component instanceof UnitGroup) {
			UnitGroupExport export = new UnitGroupExport(this);
			export.run((UnitGroup) component);
		} else if (component instanceof Actor) {
			ActorExport export = new ActorExport(this);
			export.run((Actor) component);
		} else if (component instanceof Source) {
			SourceExport export = new SourceExport(this);
			export.run((Source) component);
		}
	}
}
