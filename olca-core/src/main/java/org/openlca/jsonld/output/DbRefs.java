package org.openlca.jsonld.output;

import com.google.gson.JsonObject;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.jsonld.Json;
import org.openlca.util.Categories;
import org.openlca.util.Categories.PathBuilder;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Refs helps to create data set references when there are only model-types and
 * IDs are available (e.g. in process or provider links). For full object
 * references, {@code Json.asRef} should be used instead as this class maintains
 * an internal cache.
 */
class DbRefs {

	private final IDatabase db;
	private final PathBuilder categories;
	private final Map<ModelType, TLongObjectHashMap<? extends RootDescriptor>> cache;
	private Map<Long, String> _locationCodes;
	private Map<Long, String> _refUnits;

	private DbRefs(IDatabase db) {
		this.db = db;
		this.categories = Categories.pathsOf(db);
		this.cache = new EnumMap<>(ModelType.class);
	}

	static DbRefs of(IDatabase db) {
		return new DbRefs(db);
	}

	JsonObject asRef(RootDescriptor d) {
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

		if (d instanceof FlowDescriptor fd) {
			Json.put(ref, "flowType", (fd.flowType));
			Json.put(ref, "location", locationCodeOf(fd.location));
			Json.put(ref, "refUnit", refUnitOf(fd.refFlowPropertyId));
		}

		if (d instanceof ProcessDescriptor pd) {
			Json.put(ref, "processType", pd.processType);
			Json.put(ref, "location", pd.location);
		}

		if (d instanceof FlowPropertyDescriptor prop) {
			Json.put(ref, "refUnit", refUnitOf(prop.id));
		}

		return ref;
	}

	RootDescriptor descriptorOf(ModelType type, long id) {
		if (type == null || !type.isRoot())
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
