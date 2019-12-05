package org.openlca.io.ecospold2.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spold2.ImpactIndicator;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class MethodImport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final File[] files;
	private final IDatabase db;
	private final HashMap<String, FlowRec> flowCache = new HashMap<>();
	private UnitMapping unitMap;

	public MethodImport(File[] files, IDatabase db) {
		this.files = files;
		this.db = db;
	}

	@Override
	public void run() {
		ImpactMethodDao dao = new ImpactMethodDao(db);
		Spold2Files.parse(files, es2 -> {
			if (es2.impactMethod == null)
				return;
			ImpactMethod m = dao.getForRefId(es2.impactMethod.id);
			if (m != null) {
				log.warn("an LCIA method with id={} " +
						"already exisis; not imported", m.refId);
				return;
			}
			m = map(es2.impactMethod);
			if (m != null) {
				dao.insert(m);
				log.info("saved new LCIA method {}", m);
			}
		});
	}

	private ImpactMethod map(spold2.ImpactMethod eMethod) {
		if (eMethod.id == null) {
			log.info("method {} has no id", eMethod.name);
			return null;
		}
		log.info("import LCIA method {}", eMethod.name);
		ImpactMethod method = new ImpactMethod();
		method.refId = eMethod.id;
		method.name = eMethod.name;
		method.lastChange = new Date().getTime();
		eMethod.categories.forEach(eCategory ->
				eCategory.indicators.forEach(eIndicator -> {
					ImpactCategory impact = map(eCategory, eIndicator);
					if (impact != null) {
						method.impactCategories.add(impact);
					}
				}));
		return method;
	}

	private ImpactCategory map(spold2.ImpactCategory eCategory,
	                           ImpactIndicator eIndicator) {
		if (eCategory == null || eIndicator == null)
			return null;
		ImpactCategory impact = new ImpactCategory();
		impact.refId = KeyGen.get(eCategory.id, eIndicator.id);
		impact.name = eCategory.name + " - " + eIndicator.name;
		impact.lastChange = new Date().getTime();
		impact.referenceUnit = eIndicator.unit;
		eIndicator.factors.forEach(eFactor -> {
			ImpactFactor factor = map(eFactor);
			if (factor != null) {
				impact.impactFactors.add(factor);
			}
		});
		return impact;
	}

	private ImpactFactor map(spold2.ImpactFactor eFactor) {
		FlowRec flowRec = flow(eFactor);
		if (flowRec == null)
			return null;
		ImpactFactor f = new ImpactFactor();
		f.flow = flowRec.flow;
		f.flowPropertyFactor = flowRec.property;
		f.unit = flowRec.unit;
		f.value = eFactor.amount;
		return f;
	}

	private FlowRec flow(spold2.ImpactFactor eFactor) {
		FlowRec rec = flowCache.get(eFactor.flowID);
		if (rec != null)
			return rec;

		// try to find an existing flow with a matching unit
		Flow flow = new FlowDao(db).getForRefId(eFactor.flowID);
		if (flow != null) {
			FlowPropertyFactor property = null;
			Unit unit = null;
			for (FlowPropertyFactor f : flow.flowPropertyFactors) {
				FlowProperty prop = f.flowProperty;
				if (prop == null || prop.unitGroup == null) {
					continue;
				}
				Unit u = prop.unitGroup.getUnit(eFactor.flowUnit);
				if (u == null)
					continue;
				property = f;
				unit = u;
				if (Objects.equals(prop, flow.referenceFlowProperty))
					break;
			}
			if (unit == null) {
				log.warn("a flow {} with the ID {} exists but it " +
								"does not have a unit {}", flow.name,
						eFactor.flowID, eFactor.flowUnit);
				return null;
			}
			rec = new FlowRec(flow, property, unit);
			flowCache.put(eFactor.flowID, rec);
			return rec;
		}

		// try to create a new flow
		if (unitMap == null) {
			unitMap = UnitMapping.createDefault(db);
		}
		UnitMappingEntry e = unitMap.getEntry(eFactor.flowUnit);
		if (e == null) {
			// TODO: not sure how far we should go with the unit mapping here
			// we could even generate flow properties and unit groups for units
			// that do not exist yet but for the elementary flows this should
			// rarely be the case
			log.warn("Unknown unit {}; could not create flow {} with ID {}",
					eFactor.flowUnit, eFactor.flowName, eFactor.flowID);
			return null;
		}
		flow = new Flow();
		flow.refId = eFactor.flowID;
		flow.name = eFactor.flowName;
		flow.flowType = FlowType.ELEMENTARY_FLOW;
		if (eFactor.compartment != null) {
			flow.category = new CategoryDao(db).sync(
					ModelType.FLOW, "Elementary flows",
					eFactor.compartment.compartment,
					eFactor.compartment.subCompartment);
		}
		flow.referenceFlowProperty = e.flowProperty;
		FlowPropertyFactor property = new FlowPropertyFactor();
		property.conversionFactor = 1.0;
		property.flowProperty = e.flowProperty;
		flow.flowPropertyFactors.add(property);
		flow.lastChange = new Date().getTime();
		flow = new FlowDao(db).insert(flow);
		rec = new FlowRec(flow, property, e.unit);
		flowCache.put(eFactor.flowID, rec);
		return rec;
	}

	private static class FlowRec {
		final Flow flow;
		final FlowPropertyFactor property;
		final Unit unit;

		FlowRec(Flow flow, FlowPropertyFactor property, Unit unit) {
			this.flow = flow;
			this.property = property;
			this.unit = unit;
		}
	}
}
