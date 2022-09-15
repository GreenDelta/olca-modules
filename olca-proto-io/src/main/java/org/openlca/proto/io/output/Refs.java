package org.openlca.proto.io.output;

import com.google.protobuf.Message;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Process;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.proto.ProtoRef;
import org.openlca.util.Categories;
import org.openlca.util.Strings;

/**
 * A utility class for creating data set references.
 */
public final class Refs {

	private Refs() {
	}

	public static ProtoRef.Builder refOf(RefEntity e) {
		var proto = ProtoRef.newBuilder();
		if (e == null)
			return proto;
		proto.setType(Out.protoTypeOf(e));
		Out.map(e, proto);

		if (e instanceof Flow flow) {

			// flow specific fields
			if (flow.flowType != null) {
				proto.setFlowType(Out.flowTypeOf(flow.flowType));
			}
			if (flow.location != null) {
				proto.setLocation(Strings.orEmpty(flow.location.code));
			}
			var refUnit = flow.getReferenceUnit();
			if (refUnit != null) {
				proto.setRefUnit(Strings.orEmpty(refUnit.name));
			}

			// process specific fields
		} else if (e instanceof Process process) {
			if (process.processType != null) {
				proto.setProcessType(Out.processTypeOf(process.processType));
			}
			if (process.quantitativeReference != null) {
				var refFlow = process.quantitativeReference.flow;
				if (refFlow != null) {
					proto.setFlowType(Out.flowTypeOf(refFlow.flowType));
				}
			}
			if (process.location != null) {
				proto.setLocation(Strings.orEmpty(process.location.code));
			}

			// impact specific fields
		} else if (e instanceof ImpactCategory impact) {
			proto.setRefUnit(Strings.orEmpty(impact.referenceUnit));
		}
		return proto;
	}

	public static ProtoRef.Builder refOf(Descriptor d, RefData refData) {
		var proto = refOf(d);
		if (d == null || refData == null)
			return proto;

		if (d instanceof RootDescriptor cd) {
			proto.setCategory(refData.categoryPathOf(cd.category));
		}

		if (d instanceof ProcessDescriptor pd) {
			if (pd.location != null) {
				proto.setLocation(refData.locationCodeOf(pd.location));
			}
		}

		if (d instanceof FlowDescriptor fd) {
			if (fd.location != null) {
				proto.setLocation(refData.locationCodeOf(fd.location));
			}
			proto.setRefUnit(refData.flowUnitOf(fd.refFlowPropertyId));
		}

		return proto;
	}

	public static ProtoRef.Builder refOf(Descriptor d) {
		var proto = ProtoRef.newBuilder();
		if (d == null)
			return proto;
		proto.setType(Out.protoTypeOf(d.type));
		map(d, proto);
		if (d instanceof FlowDescriptor fd) {
			if (fd.flowType != null) {
				proto.setFlowType(Out.flowTypeOf(fd.flowType));
			}
		} else if (d instanceof ProcessDescriptor pd) {
			if (pd.processType != null) {
				proto.setProcessType(Out.processTypeOf(pd.processType));
			}
			if (pd.flowType != null) {
				proto.setFlowType((Out.flowTypeOf(pd.flowType)));
			}
		} else if (d instanceof ImpactDescriptor id) {
			proto.setRefUnit(Strings.orEmpty(id.referenceUnit));
		}
		return proto;
	}

	private static void map(Descriptor d, Message.Builder proto) {
		if (d == null || proto == null)
			return;
		var fields = proto.getDescriptorForType().getFields();
		for (var field : fields) {
			switch (field.getName()) {
				case "id" -> Out.set(proto, field, d.refId);
				case "name" -> Out.set(proto, field, d.name);
				case "version" -> Out.set(proto, field, Version.asString(d.version));
				case "last_change" ->
					Out.set(proto, field, Out.dateTimeOf(d.lastChange));
				case "library" -> Out.set(proto, field, d.library);
			}
		}
	}

	/**
	 * Returns a new ref-data object for querying reference
	 * data from the database efficiently to build data set
	 * references. You should always reuse such a ref-data
	 * object when you want to build multiple references
	 * (and the data did not change in the meantime).
	 */
	public static RefData dataOf(IDatabase db) {
		return new RefData(db);
	}

	public static class RefData {

		private final IDatabase db;
		private Categories.PathBuilder categories;
		private TLongObjectHashMap<String> flowUnits;
		private TLongObjectHashMap<String> locationCodes;

		private RefData(IDatabase db) {
			this.db = db;
		}

		String categoryPathOf(Long categoryId) {
			if (categoryId == null)
				return "";
			if (categories == null) {
				categories = Categories.pathsOf(db);
			}
			return Strings.orEmpty(categories.pathOf(categoryId));
		}

		String locationCodeOf(Long locationId) {
			if (locationId == null)
				return "";
			if (locationCodes == null) {
				locationCodes = new TLongObjectHashMap<>();
				var query = "select id, code, name from tbl_locations";
				NativeSql.on(db).query(query, r -> {
					var locID = r.getLong(1);
					var code = r.getString(2);
					if (Strings.nullOrEmpty(code)) {
						code = r.getString(3);
					}
					if (Strings.notEmpty(code)) {
						locationCodes.put(locID, code);
					}
					return true;
				});
			}
			return Strings.orEmpty(locationCodes.get(locationId));
		}

		String flowUnitOf(long flowPropertyId) {
			if (flowUnits == null) {
				flowUnits = new TLongObjectHashMap<>();
				var query = "select fp.id, u.name" +
					"  from tbl_flow_properties fp" +
					"  inner join tbl_unit_groups ug" +
					"  on fp.f_unit_group = ug.id" +
					"  inner join tbl_units u" +
					"  on ug.f_reference_unit = u.id";
				NativeSql.on(db).query(query, r -> {
					long propID = r.getLong(1);
					var unit = r.getString(2);
					flowUnits.put(propID, unit);
					return true;
				});
			}
			return Strings.orEmpty(flowUnits.get(flowPropertyId));
		}
	}
}
