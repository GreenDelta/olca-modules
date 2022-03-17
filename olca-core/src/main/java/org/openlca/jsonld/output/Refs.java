package org.openlca.jsonld.output;

import com.google.gson.JsonObject;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.jsonld.Json;
import org.openlca.util.Categories;
import org.openlca.util.Categories.PathBuilder;

import java.util.EnumMap;
import java.util.Map;

class Refs {

	private final IDatabase db;
	private final PathBuilder categories;
	private final Map<ModelType, TLongObjectHashMap<? extends RootDescriptor>> cache;
	private Map<Long, String> _locationCodes;

	private Refs(IDatabase db) {
		this.db = db;
		this.categories = Categories.pathsOf(db);
		this.cache = new EnumMap<>(ModelType.class);
	}

	static Refs of(IDatabase db) {
		return new Refs(db);
	}

	JsonObject get(ModelType type, long id) {
		var descriptor = descriptorOf(type, id);
		return get(descriptor);
	}

	JsonObject get(RootDescriptor d) {
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
			// no ref-unit required here
		}

		if (d instanceof ProcessDescriptor pd) {
			Json.put(ref, "processType", pd.processType);
			Json.put(ref, "location", pd.location);
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
}
