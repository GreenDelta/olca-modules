package org.openlca.io.ilcd.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.io.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for forwarding an export of a resource to the respective Export
 * class.
 */
class ExportDispatch {

	private static Logger log = LoggerFactory.getLogger(ExportDispatch.class);

	private ExportDispatch() {
	}

	/**
	 * Runs an export of the given model to the ILCD data store and returns the
	 * data set reference to the exported model in the store.
	 */
	public static DataSetReference forwardExportCheck(CategorizedEntity model,
			IDatabase db, DataStore target) {
		if (model instanceof Source)
			return checkRunSourceExort((Source) model, db, target);
		if (model instanceof Actor)
			return checkRunActorExport((Actor) model, db, target);
		if (model instanceof Flow)
			return checkRunFlowExport((Flow) model, db, target);
		if (model instanceof FlowProperty)
			return checkRunFlowPropertyExport((FlowProperty) model, db, target);
		if (model instanceof Process)
			return checkRunProcessExport((Process) model, db, target);
		if (model instanceof UnitGroup)
			return checkRunUnitGroupExport((UnitGroup) model, db, target);
		log.warn("Cannot export {}", model);
		return null;
	}

	private static DataSetReference checkRunSourceExort(Source source,
			IDatabase db, DataStore target) {
		try {
			if (!target.contains(org.openlca.ilcd.sources.Source.class,
					source.getRefId())) {
				SourceExport sourceExport = new SourceExport(db, target);
				sourceExport.run(source);
			}
			return DataSetRef.makeRef(source);
		} catch (Exception e) {
			log.warn("Export of source {} failed.", source);
			return null;
		}
	}

	private static DataSetReference checkRunActorExport(Actor actor,
			IDatabase database, DataStore target) {
		try {
			if (!target.contains(Contact.class, actor.getRefId())) {
				ActorExport actorExport = new ActorExport(target);
				actorExport.run(actor);
			}
			return DataSetRef.makeRef(actor);
		} catch (Exception e) {
			log.warn("Export of actor {} failed.", actor);
			return null;
		}
	}

	private static DataSetReference checkRunFlowExport(Flow flow,
			IDatabase database, DataStore target) {
		try {
			if (!target.contains(org.openlca.ilcd.flows.Flow.class,
					flow.getRefId())) {
				FlowExport flowExport = new FlowExport(database, target);
				flowExport.run(flow);
			}
			return DataSetRef.makeRef(flow);
		} catch (Exception e) {
			log.warn("Export of flow {} failed.", flow);
			return null;
		}
	}

	private static DataSetReference checkRunFlowPropertyExport(
			FlowProperty flowProperty, IDatabase database, DataStore target) {
		try {
			if (!target.contains(
					org.openlca.ilcd.flowproperties.FlowProperty.class,
					flowProperty.getRefId())) {
				FlowPropertyExport propertyExport = new FlowPropertyExport(
						database, target);
				propertyExport.run(flowProperty);
			}
			return DataSetRef.makeRef(flowProperty);
		} catch (Exception e) {
			log.warn("Export of flow property {} failed.", flowProperty);
			return null;
		}
	}

	private static DataSetReference checkRunUnitGroupExport(
			UnitGroup unitGroup, IDatabase database, DataStore target) {
		try {
			if (!target.contains(org.openlca.ilcd.units.UnitGroup.class,
					unitGroup.getRefId())) {
				UnitGroupExport export = new UnitGroupExport(target);
				export.run(unitGroup);
			}
			return DataSetRef.makeRef(unitGroup);
		} catch (Exception e) {
			log.warn("Export of unit group {} failed.", unitGroup);
			return null;
		}
	}

	private static DataSetReference checkRunProcessExport(Process process,
			IDatabase database, DataStore target) {
		try {
			if (!target.contains(org.openlca.ilcd.processes.Process.class,
					process.getRefId())) {
				ProcessExport export = new ProcessExport(database, target);
				export.run(process);
			}
			return DataSetRef.makeRef(process);
		} catch (Exception e) {
			log.warn("Export of process {} failed.", process);
			return null;
		}
	}

}
