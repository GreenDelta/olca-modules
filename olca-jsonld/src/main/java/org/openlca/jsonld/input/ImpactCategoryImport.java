package org.openlca.jsonld.input;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.jsonld.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ImpactCategoryImport extends
		BaseEmbeddedImport<ImpactCategory, ImpactMethod> {

	/**
	 * This is an experimental feature. LCIA categories are not yet stand-alone
	 * objects in openLCA but attached to LCIA methods. However, in our JSON-LD
	 * format they are not stored within the LCIA method data sets. Thus, when we
	 * want to update a LCIA category, e.g. over the IPC server, we want to directly
	 * call the update on an LCIA category object which then should update the
	 * respective LCIA method. Note that this function needs to be changed when we
	 * move to stand-alone LCIA categories in openLCA.
	 */
	public static void run(String impactCategoryID, ImportConfig conf) {
		if (impactCategoryID == null || conf == null)
			return;
		JsonObject json = conf.store.get(
				ModelType.IMPACT_CATEGORY, impactCategoryID);
		if (json == null)
			return;

		IDatabase db = conf.db.getDatabase();
		ImpactCategoryDao catDao = new ImpactCategoryDao(db);

		// check whether the LCIA category is already attached to an LCIA method
		AtomicReference<String> methodIDRef = new AtomicReference<>();
		ImpactCategoryDescriptor des = catDao.getDescriptorForRefId(impactCategoryID);
		if (des != null) {
			String sql = "select m.ref_id from tbl_impact_categories c inner "
					+ "join tbl_impact_methods m on c.f_impact_method = m.id where "
					+ "c.id = " + des.id;
			try {
				NativeSql.on(db).query(sql, r -> {
					methodIDRef.set(r.getString(1));
					return false;
				});
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(ImpactCategoryImport.class);
				log.error("Failed to get LCIA method", e);
			}
		}

		// the import should also work when there is no method
		String methodID = methodIDRef.get();
		ImpactCategoryImport imp = new ImpactCategoryImport(
				methodID, conf);
		ImpactCategory impact = imp.run(json);
		if (impact == null)
			return;

		if (methodID != null) {
			ImpactMethodDao dao = new ImpactMethodDao(db);
			ImpactMethod method = dao.getForRefId(methodID);
			ImpactCategory old = imp.getPersisted(method, json);
			if (old != null) {
				method.impactCategories.remove(old);
			}
			method.impactCategories.add(impact);
			dao.update(method);
		} else {
			if (impact.id == 0L) {
				catDao.insert(impact);
			} else {
				catDao.update(impact);
			}
		}
	}

	ImpactCategoryImport(String methodID, ImportConfig conf) {
		super(ModelType.IMPACT_METHOD, methodID, conf);
	}

	@Override
	ImpactCategory getPersisted(ImpactMethod method, JsonObject json) {
		if (method == null)
			return null;
		String refId = Json.getString(json, "@id");
		if (refId == null)
			return null;
		for (ImpactCategory cat : method.impactCategories) {
			if (refId.equals(cat.refId))
				return cat;
		}
		return null;
	}

	@Override
	ImpactCategory map(JsonObject json, long id) {
		if (json == null)
			return null;
		ImpactCategory cat = new ImpactCategory();
		In.mapAtts(json, cat, id);
		cat.referenceUnit = Json.getString(json, "referenceUnitName");
		JsonArray factors = Json.getArray(json, "impactFactors");
		if (factors == null || factors.size() == 0)
			return cat;
		for (JsonElement e : factors) {
			if (!e.isJsonObject())
				continue;
			ImpactFactor factor = mapFactor(e.getAsJsonObject(), conf);
			if (factor == null)
				continue;
			cat.impactFactors.add(factor);
		}
		return cat;
	}

	private ImpactFactor mapFactor(JsonObject json, ImportConfig conf) {
		if (json == null || conf == null)
			return null;

		ImpactFactor factor = new ImpactFactor();
		factor.value = Json.getDouble(json, "value", 0);
		factor.formula = Json.getString(json, "formula");
		String flowId = Json.getRefId(json, "flow");
		Flow flow = FlowImport.run(flowId, conf);
		factor.flow = flow;
		if (flow == null) {
			conf.log.warn("invalid flow {}; LCIA factor not imported", flowId);
			return null;
		}

		JsonElement uncertainty = json.get("uncertainty");
		if (uncertainty != null && uncertainty.isJsonObject()) {
			factor.uncertainty = Uncertainties.read(
					uncertainty.getAsJsonObject());
		}

		// set the flow property and unit; if we cannot find them
		// we will choose the reference data from the flow
		// when we cannot find consistent information we return
		// a factor where the unit or flow property factor may
		// is absent.
		Unit unit = conf.db.get(ModelType.UNIT, Json.getRefId(json, "unit"));
		FlowPropertyFactor propFac = getPropertyFactor(json, flow);
		if (unit != null && propFac != null) {
			factor.unit = unit;
			factor.flowPropertyFactor = propFac;
			return factor;
		}

		if (propFac == null) {
			propFac = flow.getReferenceFactor();
			if (propFac == null || propFac.flowProperty == null)
				return factor;
		}
		factor.flowPropertyFactor = propFac;

		UnitGroup ug = propFac.flowProperty.unitGroup;
		if (ug == null)
			return factor;

		if (unit == null) {
			factor.unit = ug.referenceUnit;
		} else {
			for (Unit u : ug.units) {
				if (Objects.equals(u, unit)) {
					factor.unit = u;
				}
			}
		}
		return factor;
	}

	private FlowPropertyFactor getPropertyFactor(JsonObject json,
			Flow flow) {
		if (json == null || flow == null)
			return null;
		String propId = Json.getRefId(json, "flowProperty");
		for (FlowPropertyFactor fac : flow.flowPropertyFactors) {
			FlowProperty prop = fac.flowProperty;
			if (prop == null)
				continue;
			if (Objects.equals(propId, prop.refId))
				return fac;
		}
		return null;
	}
}
