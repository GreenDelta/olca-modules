package org.openlca.io.ilcd.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
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
	public static Ref forwardExportCheck(CategorizedEntity model,
			ExportConfig config) {
		if (model instanceof Source)
			return checkRunSourceExort((Source) model, config);
		if (model instanceof Actor)
			return checkRunActorExport((Actor) model, config);
		if (model instanceof Flow)
			return checkRunFlowExport((Flow) model, config);
		if (model instanceof FlowProperty)
			return checkRunFlowPropertyExport((FlowProperty) model, config);
		if (model instanceof Process)
			return checkRunProcessExport((Process) model, config);
		if (model instanceof UnitGroup)
			return checkRunUnitGroupExport((UnitGroup) model, config);
		log.error("Cannot export {}", model);
		return null;
	}

	private static Ref checkRunSourceExort(Source source,
			ExportConfig config) {
		try {
			if (!config.store.contains(org.openlca.ilcd.sources.Source.class,
					source.getRefId())) {
				SourceExport sourceExport = new SourceExport(config);
				sourceExport.run(source);
			}
			return DataSetRef.makeRef(source, config);
		} catch (Exception e) {
			log.error("Export of source failed: " + source, e);
			return null;
		}
	}

	private static Ref checkRunActorExport(Actor actor,
			ExportConfig config) {
		try {
			if (!config.store.contains(Contact.class, actor.getRefId())) {
				ActorExport actorExport = new ActorExport(config);
				actorExport.run(actor);
			}
			return DataSetRef.makeRef(actor, config);
		} catch (Exception e) {
			log.error("Export of actor failed: " + actor, e);
			return null;
		}
	}

	private static Ref checkRunFlowExport(Flow flow,
			ExportConfig config) {
		try {
			if (!config.store.contains(org.openlca.ilcd.flows.Flow.class,
					flow.getRefId())) {
				FlowExport flowExport = new FlowExport(config);
				flowExport.run(flow);
			}
			return DataSetRef.makeRef(flow, config);
		} catch (Exception e) {
			log.error("Export of flow failed: " + flow, e);
			return null;
		}
	}

	private static Ref checkRunFlowPropertyExport(
			FlowProperty flowProperty, ExportConfig config) {
		try {
			if (!config.store.contains(
					org.openlca.ilcd.flowproperties.FlowProperty.class,
					flowProperty.getRefId())) {
				FlowPropertyExport propertyExport = new FlowPropertyExport(
						config);
				propertyExport.run(flowProperty);
			}
			return DataSetRef.makeRef(flowProperty, config);
		} catch (Exception e) {
			log.error("Export of flow property failed: " + flowProperty, e);
			return null;
		}
	}

	private static Ref checkRunUnitGroupExport(
			UnitGroup unitGroup, ExportConfig config) {
		try {
			if (!config.store.contains(org.openlca.ilcd.units.UnitGroup.class,
					unitGroup.getRefId())) {
				UnitGroupExport export = new UnitGroupExport(config);
				export.run(unitGroup);
			}
			return DataSetRef.makeRef(unitGroup, config);
		} catch (Exception e) {
			log.error("Export of unit group failed: " + unitGroup, e);
			return null;
		}
	}

	private static Ref checkRunProcessExport(Process process,
			ExportConfig config) {
		try {
			if (!config.store.contains(
					org.openlca.ilcd.processes.Process.class,
					process.getRefId())) {
				ProcessExport export = new ProcessExport(config);
				export.run(process);
			}
			return DataSetRef.makeRef(process, config);
		} catch (Exception e) {
			log.error("Export of process failed: " + process, e);
			return null;
		}
	}

}
