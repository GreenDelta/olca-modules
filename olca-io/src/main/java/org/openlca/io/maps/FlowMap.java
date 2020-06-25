package org.openlca.io.maps;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ParseDouble;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FlowMap extends Descriptor {

	/** Description of the source system. */
	public Descriptor source;

	/** Description of the target system. */
	public Descriptor target;

	public final List<FlowMapEntry> entries = new ArrayList<>();

	private Map<String, FlowMapEntry> index;

	public static FlowMap empty() {
		return new FlowMap();
	}

	/**
	 * Get the mapping entry for the source flow with the given ID.
	 */
	public FlowMapEntry getEntry(String sourceFlowID) {
		if (index == null) {
			index = new HashMap<>();
			for (var e : entries) {
				var sid = e.sourceFlowID();
				if (sid != null) {
					index.put(sid, e);
				}
			}
		}
		return index.get(sourceFlowID);
	}

	/**
	 * Reads the flow map with the given identifier from this package or the
	 * given database.
	 *
	 * @deprecated we should remove implicit mappings that are loaded from this
	 *             package and also loading mappings from the database
	 */
	@Deprecated
	public static FlowMap of(String map, IDatabase db) {
		Logger log = LoggerFactory.getLogger(FlowMap.class);
		log.trace("Initialize flow assignment map {}.", map);
		FlowMap m = new FlowMap();
		m.name = map;
		try {
			var dbIDs = new HashSet<>();
			new FlowDao(db).getDescriptors().forEach(d -> dbIDs.add(d.refId));
			Maps.readAll(map, db, null, null, new ParseDouble()).forEach(r -> {
				String sourceID = Maps.getString(r, 0);
				String targetID = Maps.getString(r, 1);
				if (targetID == null || !dbIDs.contains(targetID))
					return;
				FlowMapEntry e = new FlowMapEntry();
				e.sourceFlow = new FlowRef();
				e.sourceFlow.flow = new FlowDescriptor();
				e.sourceFlow.flow.refId = sourceID;
				e.targetFlow = new FlowRef();
				e.targetFlow.flow = new FlowDescriptor();
				e.targetFlow.flow.refId = targetID;
				e.factor = Maps.getDouble(r, 2);
				m.entries.add(e);
			});
		} catch (Exception e) {
			log.error("Error while reading mapping file", e);
		}
		return m;
	}

	public static FlowMap fromCsv(File file) {
		FlowMap fm = new FlowMap();
		if (file == null)
			return fm;
		fm.name = file.getName();
		Maps.each(file, row -> {

			FlowMapEntry e = new FlowMapEntry();
			fm.entries.add(e);
			e.factor = Maps.getDouble(row, 2);

			// source flow
			String sid = Maps.getString(row, 0);
			if (Strings.notEmpty(sid)) {
				e.sourceFlow = new FlowRef();
				e.sourceFlow.flow = new FlowDescriptor();
				e.sourceFlow.flow.refId = sid;
				e.sourceFlow.flow.name = Maps.getString(row, 3);
				e.sourceFlow.flowCategory = Maps.getString(row, 4);
				e.sourceFlow.flowLocation = Maps.getString(row, 5);

				// flow property
				String sprop = Maps.getString(row, 9);
				if (Strings.notEmpty(sprop)) {
					e.sourceFlow.property = new FlowPropertyDescriptor();
					e.sourceFlow.property.refId = sprop;
					e.sourceFlow.property.name = Maps.getString(row, 10);
				}

				// unit
				String sunitID = Maps.getString(row, 13);
				String sunitName = Maps.getString(row, 14);
				if (Strings.notEmpty(sunitID) || Strings.notEmpty(sunitName)) {
					e.sourceFlow.unit = new UnitDescriptor();
					e.sourceFlow.unit.refId = sunitID;
					e.sourceFlow.unit.name = sunitName;
				}

				// status
				String sstatus = Maps.getString(row, 21);
				if (Strings.notEmpty(sstatus)) {
					e.sourceFlow.status = Status.fromString(sstatus);
				}
			}

			// target flow
			String tid = Maps.getString(row, 1);
			if (Strings.notEmpty(tid)) {
				e.targetFlow = new FlowRef();
				e.targetFlow.flow = new FlowDescriptor();
				e.targetFlow.flow.refId = tid;
				e.targetFlow.flow.name = Maps.getString(row, 6);
				e.targetFlow.flowCategory = Maps.getString(row, 7);
				e.targetFlow.flowLocation = Maps.getString(row, 8);

				// flow property
				String tprop = Maps.getString(row, 11);
				if (Strings.notEmpty(tprop)) {
					e.targetFlow.property = new FlowPropertyDescriptor();
					e.targetFlow.property.refId = tprop;
					e.targetFlow.property.name = Maps.getString(row, 12);
				}

				// unit
				String tunitID = Maps.getString(row, 15);
				String tunitName = Maps.getString(row, 16);
				if (Strings.notEmpty(tunitID) || Strings.notEmpty(tunitName)) {
					e.targetFlow.unit = new UnitDescriptor();
					e.targetFlow.unit.refId = tunitID;
					e.targetFlow.unit.name = tunitName;
				}

				// provider
				String prov = Maps.getString(row, 17);
				if (Strings.notEmpty(prov)) {
					e.targetFlow.provider = new ProcessDescriptor();
					e.targetFlow.provider.refId = prov;
					e.targetFlow.provider.name = Maps.getString(row, 18);
					e.targetFlow.providerCategory = Maps.getString(row, 19);
					e.targetFlow.providerLocation = Maps.getString(row, 20);
				}

				// status
				String tstatus = Maps.getString(row, 22);
				if (Strings.notEmpty(tstatus)) {
					e.targetFlow.status = Status.fromString(tstatus);
				}
			}
		});
		return fm;
	}

	public static void toCsv(FlowMap fm, File file) {
		if (fm == null || file == null)
			return;
		Maps.write(file, fm.entries.stream().map(e -> {
			Object[] row = new Object[23];
			row[2] = e.factor;

			// source flow
			FlowRef s = e.sourceFlow;
			if (s != null) {

				// flow
				if (s.flow != null) {
					row[0] = s.flow.refId;
					row[3] = s.flow.name;
					row[4] = s.flowCategory;
					row[5] = s.flowLocation;
				}

				// flow property
				if (s.property != null) {
					row[9] = s.property.refId;
					row[10] = s.property.name;
				}

				// unit
				if (s.unit != null) {
					row[13] = s.unit.refId;
					row[14] = s.unit.name;
				}

				// status
				if (s.status != null) {
					row[21] = s.status.toString();
				}

			}

			// target flow
			FlowRef t = e.targetFlow;
			if (t != null) {

				// flow
				if (t.flow != null) {
					row[1] = t.flow.refId;
					row[6] = t.flow.name;
					row[7] = t.flowCategory;
					row[8] = t.flowLocation;
				}

				// flow property
				if (t.property != null) {
					row[11] = t.property.refId;
					row[12] = t.property.name;
				}

				// unit
				if (t.unit != null) {
					row[15] = t.unit.refId;
					row[16] = t.unit.name;
				}

				// provider
				if (t.provider != null) {
					row[17] = t.provider.refId;
					row[18] = t.provider.name;
					row[19] = t.providerCategory;
					row[20] = t.providerLocation;
				}

				// status
				if (t.status != null) {
					row[22] = t.status.toString();
				}
			}
			return row;
		}));
	}

	public static FlowMap fromJson(JsonObject obj) {
		FlowMap map = new FlowMap();
		map.name = Json.getString(obj, "name");
		map.description = Json.getString(obj, "name");

		map.source = new Descriptor();
		mapDescriptor(Json.getObject(obj, "source"), map.source);
		map.target = new Descriptor();
		mapDescriptor(Json.getObject(obj, "target"), map.target);

		JsonArray array = Json.getArray(obj, "mappings");
		if (array != null) {
			for (JsonElement e : array) {
				if (!e.isJsonObject())
					continue;
				JsonObject eObj = e.getAsJsonObject();
				FlowMapEntry entry = new FlowMapEntry();
				entry.sourceFlow = asFlowRef(
						Json.getObject(eObj, "from"));
				entry.targetFlow = asFlowRef(
						Json.getObject(eObj, "to"));
				entry.factor = Json.getDouble(
						eObj, "conversionFactor", 1.0);
				map.entries.add(entry);
			}
		}
		return map;
	}

	private static FlowRef asFlowRef(JsonObject obj) {
		if (obj == null)
			return null;
		FlowRef ref = new FlowRef();
		ref.flow = new FlowDescriptor();
		JsonObject flowObj = Json.getObject(obj, "flow");
		if (flowObj == null)
			return null;

		mapDescriptor(flowObj, ref.flow);
		ref.flowCategory = categoryPath(flowObj);

		JsonObject fp = Json.getObject(obj, "flowProperty");
		if (fp != null) {
			ref.property = new Descriptor();
			mapDescriptor(fp, ref.property);
		}
		JsonObject u = Json.getObject(obj, "unit");
		if (u != null) {
			ref.unit = new Descriptor();
			mapDescriptor(u, ref.unit);
		}

		return ref;
	}

	private static void mapDescriptor(JsonObject obj, Descriptor d) {
		if (obj == null || d == null)
			return;
		d.name = Json.getString(obj, "name");
		d.description = Json.getString(obj, "description");
		d.refId = Json.getString(obj, "@id");
		if (d instanceof FlowDescriptor) {
			FlowDescriptor fd = (FlowDescriptor) d;
			fd.flowType = Json.getEnum(obj, "flowType", FlowType.class);
			if (fd.flowType == null) {
				fd.flowType = FlowType.ELEMENTARY_FLOW;
			}
		}
	}

	private static String categoryPath(JsonObject obj) {
		if (obj == null)
			return null;
		JsonArray array = Json.getArray(obj, "categoryPath");
		if (array == null)
			return null;
		StringBuilder path = new StringBuilder();
		for (JsonElement elem : array) {
			if (!elem.isJsonPrimitive())
				continue;
			if (path.length() > 0) {
				path.append("/");
			}
			path.append(elem.getAsString());
		}
		return path.toString();
	}
}
