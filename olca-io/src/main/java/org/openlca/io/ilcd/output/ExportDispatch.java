package org.openlca.io.ilcd.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.Ref;
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
	public static Ref forwardExport(CategorizedEntity model,
			ExportConfig config) {
		if (model instanceof Source)
			return sourceExort((Source) model, config);
		if (model instanceof Actor)
			return actorExport((Actor) model, config);
		if (model instanceof Flow)
			return flowExport((Flow) model, config);
		if (model instanceof FlowProperty)
			return flowPropertyExport((FlowProperty) model, config);
		if (model instanceof Process)
			return processExport((Process) model, config);
		if (model instanceof UnitGroup)
			return unitGroupExport((UnitGroup) model, config);
		log.error("Cannot export {}", model);
		return null;
	}

	private static Ref sourceExort(Source source, ExportConfig config) {
		try {
			SourceExport sourceExport = new SourceExport(config);
			sourceExport.run(source);
			return DataSetRef.makeRef(source, config);
		} catch (Exception e) {
			log.error("Export of source failed: " + source, e);
			return null;
		}
	}

	private static Ref actorExport(Actor actor, ExportConfig config) {
		try {
			ActorExport actorExport = new ActorExport(config);
			actorExport.run(actor);
			return DataSetRef.makeRef(actor, config);
		} catch (Exception e) {
			log.error("Export of actor failed: " + actor, e);
			return null;
		}
	}

	private static Ref flowExport(Flow flow, ExportConfig config) {
		try {
			FlowExport flowExport = new FlowExport(config);
			flowExport.run(flow);
			return DataSetRef.makeRef(flow, config);
		} catch (Exception e) {
			log.error("Export of flow failed: " + flow, e);
			return null;
		}
	}

	private static Ref flowPropertyExport(FlowProperty prop,
			ExportConfig config) {
		try {
			FlowPropertyExport export = new FlowPropertyExport(config);
			export.run(prop);
			return DataSetRef.makeRef(prop, config);
		} catch (Exception e) {
			log.error("Export of flow property failed: " + prop, e);
			return null;
		}
	}

	private static Ref unitGroupExport(UnitGroup unitGroup,
			ExportConfig config) {
		try {
			UnitGroupExport export = new UnitGroupExport(config);
			export.run(unitGroup);
			return DataSetRef.makeRef(unitGroup, config);
		} catch (Exception e) {
			log.error("Export of unit group failed: " + unitGroup, e);
			return null;
		}
	}

	private static Ref processExport(Process process, ExportConfig config) {
		try {
			ProcessExport export = new ProcessExport(config);
			export.run(process);
			return DataSetRef.makeRef(process, config);
		} catch (Exception e) {
			log.error("Export of process failed: " + process, e);
			return null;
		}
	}

}
