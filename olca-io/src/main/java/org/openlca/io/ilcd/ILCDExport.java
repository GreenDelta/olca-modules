package org.openlca.io.ilcd;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.io.ZipStore;
import org.openlca.io.ilcd.output.ActorExport;
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

	public void export(CategorizedEntity component, IDatabase database) {
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

	private void tryExport(CategorizedEntity component, IDatabase database)
			throws Exception {

		if (component instanceof ImpactMethod) {
			ImpactMethodExport export = new ImpactMethodExport(database,
					ilcdStore);
			export.run((ImpactMethod) component);

		} else if (component instanceof ProductSystem) {
			SystemExport export = new SystemExport(database, ilcdStore);
			export.run((ProductSystem) component);

		} else if (component instanceof Process) {
			ProcessExport export = new ProcessExport(database, ilcdStore);
			export.run((Process) component);

		} else if (component instanceof Flow) {
			FlowExport flowExport = new FlowExport(database, ilcdStore);
			flowExport.run((Flow) component);

		} else if (component instanceof FlowProperty) {
			FlowPropertyExport export = new FlowPropertyExport(database,
					ilcdStore);
			export.run((FlowProperty) component);

		} else if (component instanceof UnitGroup) {
			UnitGroupExport export = new UnitGroupExport(ilcdStore);
			export.run((UnitGroup) component);

		} else if (component instanceof Actor) {
			ActorExport export = new ActorExport(ilcdStore);
			export.run((Actor) component);

		} else if (component instanceof Source) {
			SourceExport export = new SourceExport(database, ilcdStore);
			export.run((Source) component);
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
