package org.openlca.jsonld.output;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.IDatabase.DataPackages;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.jsonld.Json;
import org.openlca.util.Categories;
import org.openlca.util.Categories.PathBuilder;

import com.google.gson.JsonObject;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * JsonRefs helps to create data set references when no full-entities are
 * available. An instance of this class maintains an internal cache and can be
 * reused when multiple references should be created. For full-entities,
 * {@code Json.asRef} could be used instead.
 */
public class JsonRefs {

	private final IDatabase db;
	private final PathBuilder categories;
	private final Map<ModelType, TLongObjectHashMap<? extends RootDescriptor>> cache;
	private final DataPackages dataPackages;
	private Map<Long, String> _locationCodes;
	private Map<Long, String> _refUnits;
	private boolean writeDataPackageFields;

	private JsonRefs(IDatabase db) {
		this.db = db;
		this.categories = Categories.pathsOf(db);
		this.cache = new EnumMap<>(ModelType.class);
		this.dataPackages = db.getDataPackages();
	}

	public static JsonRefs of(IDatabase db) {
		return new JsonRefs(db);
	}

	/// If set to `true`, created references will contain the `dataPackage` field
	/// when the referenced dataset belongs to a data package. Typically, this should
	/// be only done when references are exported to a service API and not in
	/// the standard JSON exports.
	public JsonRefs withDataPackageFields(boolean b) {
		writeDataPackageFields = b;
		return this;
	}

	public JsonObject asRef(RootDescriptor d) {
		if (d == null)
			return null;
		var ref = new JsonObject();

		// @type
		if (d.type != null) {
			var clazz = d.type.getModelClass();
			if (clazz != null) {
				Json.put(ref, "@type", clazz.getSimpleName());
			}
		}

		Json.put(ref, "@id", d.refId);
		Json.put(ref, "name", d.name);
		Json.put(ref, "category", categories.pathOf(d.category));

		if (writeDataPackageFields) {
			var dataPackage = dataPackages.get(d.dataPackage);
			if (dataPackage != null) {
				// TODO is support of legacy field name required?
				if (dataPackage.isLibrary()) {
					Json.put(ref, "library", dataPackage.name());
				} else {
					Json.put(ref, "dataPackage", dataPackage.name());
				}
			}
		}

		if (d instanceof FlowDescriptor fd) {
			Json.put(ref, "flowType", (fd.flowType));
			Json.put(ref, "location", locationCodeOf(fd.location));
			Json.put(ref, "refUnit", refUnitOf(fd.refFlowPropertyId));
		}

		if (d instanceof ProcessDescriptor pd) {
			Json.put(ref, "processType", pd.processType);
			Json.put(ref, "flowType", pd.flowType);
			Json.put(ref, "location", locationCodeOf(pd.location));
		}

		if (d instanceof FlowPropertyDescriptor prop) {
			Json.put(ref, "refUnit", refUnitOf(prop.id));
		}

		if (d instanceof ImpactDescriptor impact) {
			Json.put(ref, "refUnit", impact.referenceUnit);
		}

		return ref;
	}

	public JsonObject asRef(ModelType type, long id) {
		var d = descriptorOf(type, id);
		return asRef(d);
	}

	RootDescriptor descriptorOf(ModelType type, long id) {
		if (type == null)
			return null;
		var map = cache.computeIfAbsent(
				type, _type -> Daos.root(db, type).descriptorMap());
		return map.get(id);
	}

	private String locationCodeOf(Long id) {
		if (id == null)
			return null;
		if (_locationCodes == null) {
			_locationCodes = new LocationDao(db).getCodes();
		}
		return _locationCodes.get(id);
	}

	private String refUnitOf(Long propId) {
		if (propId == null)
			return null;
		if (_refUnits == null) {
			_refUnits = new HashMap<>();
			var query = """
					select prop.id, unit.name from tbl_flow_properties prop
					  inner join tbl_unit_groups as ug on prop.f_unit_group = ug.id
					  inner join tbl_units unit on ug.f_reference_unit = unit.id
					""";
			NativeSql.on(db).query(query, r -> {
				_refUnits.put(r.getLong(1), r.getString(2));
				return true;
			});
		}
		return _refUnits.get(propId);
	}
}
