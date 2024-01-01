package org.openlca.io.ilcd.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Epd;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.DataSetType;
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
	public static Ref of(RootEntity model, ILCDExport exp) {
		if (model instanceof Source)
			return source((Source) model, exp);
		if (model instanceof Actor)
			return actor((Actor) model, exp);
		if (model instanceof Flow)
			return flow((Flow) model, exp);
		if (model instanceof FlowProperty)
			return property((FlowProperty) model, exp);
		if (model instanceof Process)
			return process((Process) model, exp);
		if (model instanceof UnitGroup)
			return unitGroup((UnitGroup) model, exp);
		log.error("Cannot export {}", model);
		return null;
	}

	private static Ref source(Source source, ILCDExport config) {
		try {
			var export = new SourceExport(config);
			export.run(source);
			return refOf(source, config);
		} catch (Exception e) {
			log.error("Export of source failed: " + source, e);
			return null;
		}
	}

	private static Ref actor(Actor actor, ILCDExport config) {
		try {
			var export = new ActorExport(config);
			export.run(actor);
			return refOf(actor, config);
		} catch (Exception e) {
			log.error("Export of actor failed: " + actor, e);
			return null;
		}
	}

	private static Ref flow(Flow flow, ILCDExport config) {
		try {
			var export = new FlowExport(config);
			export.run(flow);
			return refOf(flow, config);
		} catch (Exception e) {
			log.error("Export of flow failed: " + flow, e);
			return null;
		}
	}

	private static Ref property(FlowProperty prop, ILCDExport config) {
		try {
			var export = new FlowPropertyExport(config);
			export.run(prop);
			return refOf(prop, config);
		} catch (Exception e) {
			log.error("Export of flow property failed: " + prop, e);
			return null;
		}
	}

	private static Ref unitGroup(UnitGroup unitGroup, ILCDExport config) {
		try {
			var export = new UnitGroupExport(config);
			export.run(unitGroup);
			return refOf(unitGroup, config);
		} catch (Exception e) {
			log.error("Export of unit group failed: " + unitGroup, e);
			return null;
		}
	}

	private static Ref process(Process process, ILCDExport config) {
		try {
			var export = new ProcessExport(config);
			export.run(process);
			return refOf(process, config);
		} catch (Exception e) {
			log.error("Export of process failed: " + process, e);
			return null;
		}
	}

	public static Ref refOf(RootEntity e, ILCDExport exp) {
		if (e == null) {
			return new Ref();
		}
		var ref = new Ref();
		ref.version = "01.00.000";
		exp.add(ref.name, e.name);
		ref.uuid = e.refId;
		ref.type = refTypeOf(e);
		ref.uri = "../" + pathOf(ref.type) + "/" + e.refId + ".xml";
		return ref;
	}

	private static DataSetType refTypeOf(RootEntity e) {
		if (e instanceof Actor)
			return DataSetType.CONTACT;
		if (e instanceof Source)
			return DataSetType.SOURCE;
		if (e instanceof UnitGroup)
			return DataSetType.UNIT_GROUP;
		if (e instanceof FlowProperty)
			return DataSetType.FLOW_PROPERTY;
		if (e instanceof Flow)
			return DataSetType.FLOW;
		if (e instanceof ImpactCategory)
			return DataSetType.LCIA_METHOD;
		if (e instanceof Process || e instanceof Epd)
			return DataSetType.PROCESS;
		if (e instanceof ProductSystem)
			return DataSetType.MODEL;
		return null;
	}

	private static String pathOf(DataSetType type) {
		if (type == null)
			return "?";
		return switch (type) {
			case CONTACT -> "contacts";
			case SOURCE -> "sources";
			case UNIT_GROUP -> "unitgroups";
			case FLOW_PROPERTY -> "flowproperties";
			case FLOW -> "flows";
			case PROCESS -> "processes";
			case MODEL -> "lifecyclemodels";
			case LCIA_METHOD -> "lciamethods";
			case EXTERNAL_FILE -> "external_docs";
		};
	}

}
