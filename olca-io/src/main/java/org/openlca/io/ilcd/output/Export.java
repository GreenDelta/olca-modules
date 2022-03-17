package org.openlca.io.ilcd.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.RootEntity;
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
class Export {

	private static final Logger log = LoggerFactory.getLogger(Export.class);

	private Export() {
	}

	/**
	 * Runs an export of the given model to the ILCD data store and returns the
	 * data set reference to the exported model in the store.
	 */
	public static Ref of(RootEntity model, ExportConfig config) {
		if (model instanceof Source)
			return source((Source) model, config);
		if (model instanceof Actor)
			return actor((Actor) model, config);
		if (model instanceof Flow)
			return flow((Flow) model, config);
		if (model instanceof FlowProperty)
			return property((FlowProperty) model, config);
		if (model instanceof Process)
			return process((Process) model, config);
		if (model instanceof UnitGroup)
			return unitGroup((UnitGroup) model, config);
		log.error("Cannot export {}", model);
		return null;
	}

	private static Ref source(Source source, ExportConfig config) {
		try {
			var export = new SourceExport(config);
			export.run(source);
			return DataSetRef.makeRef(source, config);
		} catch (Exception e) {
			log.error("Export of source failed: " + source, e);
			return null;
		}
	}

	private static Ref actor(Actor actor, ExportConfig config) {
		try {
			var export = new ActorExport(config);
			export.run(actor);
			return DataSetRef.makeRef(actor, config);
		} catch (Exception e) {
			log.error("Export of actor failed: " + actor, e);
			return null;
		}
	}

	private static Ref flow(Flow flow, ExportConfig config) {
		try {
			var export = new FlowExport(config);
			export.run(flow);
			return DataSetRef.makeRef(flow, config);
		} catch (Exception e) {
			log.error("Export of flow failed: " + flow, e);
			return null;
		}
	}

	private static Ref property(FlowProperty prop, ExportConfig config) {
		try {
			var export = new FlowPropertyExport(config);
			export.run(prop);
			return DataSetRef.makeRef(prop, config);
		} catch (Exception e) {
			log.error("Export of flow property failed: " + prop, e);
			return null;
		}
	}

	private static Ref unitGroup(UnitGroup unitGroup, ExportConfig config) {
		try {
			var export = new UnitGroupExport(config);
			export.run(unitGroup);
			return DataSetRef.makeRef(unitGroup, config);
		} catch (Exception e) {
			log.error("Export of unit group failed: " + unitGroup, e);
			return null;
		}
	}

	private static Ref process(Process process, ExportConfig config) {
		try {
			var export = new ProcessExport(config);
			export.run(process);
			return DataSetRef.makeRef(process, config);
		} catch (Exception e) {
			log.error("Export of process failed: " + process, e);
			return null;
		}
	}

}
