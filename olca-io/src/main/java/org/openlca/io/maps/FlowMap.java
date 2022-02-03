package org.openlca.io.maps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.MappingFile;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;
import org.openlca.jsonld.Json;
import org.openlca.util.BinUtils;
import org.openlca.util.Strings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FlowMap extends Descriptor {

	/**
	 * Description of the source system.
	 */
	public Descriptor source;

	/**
	 * Description of the target system.
	 */
	public Descriptor target;

	public final List<FlowMapEntry> entries = new ArrayList<>();

	public static FlowMap empty() {
		return new FlowMap();
	}

	/**
	 * Constructs a flow map from the openLCA internal storage format.
	 */
	public static FlowMap of(MappingFile mapping) {
		if (mapping == null)
			return empty();
		if (mapping.content == null) {
			var fm = empty();
			fm.name = mapping.name;
			return fm;
		}
		var fm = fromCsv(mapping.content);
		fm.name = mapping.name;
		return fm;
	}

	/**
	 * Creates an index of the entries in this flow map.
	 *
	 * @return a map where the IDs of the source flows are mapped to the
	 * respective mapping entries.
	 */
	public Map<String, FlowMapEntry> index() {
		var index = new HashMap<String, FlowMapEntry>();
		for (var e : entries) {
			var sourceId = e.sourceFlowId();
			if (sourceId != null) {
				index.put(sourceId, e);
			}
		}
		return index;
	}

	public static FlowMap fromCsv(byte[] bytes) {
		if (bytes == null)
			return new FlowMap();
		var data = BinUtils.isGzip(bytes)
			? BinUtils.gunzip(bytes)
			: bytes;
		var stream = new ByteArrayInputStream(data);
		return fromCsv(stream);
	}

	public static FlowMap fromCsv(File file) {
		if (file == null)
			return new FlowMap();
		try (var stream = new FileInputStream(file)) {
			var map = fromCsv(stream);
			map.name = file.getName();
			return map;
		} catch (IOException e) {
			throw new RuntimeException("Failed to " +
				"read flow map from " + file, e);
		}
	}

	public static FlowMap fromCsv(InputStream stream) {
		FlowMap fm = new FlowMap();
		if (stream == null)
			return fm;
		Maps.each(stream, row -> {

			// source flow
			FlowRef sourceFlow = null;
			String sid = Maps.getString(row, 0);
			if (Strings.notEmpty(sid)) {
				sourceFlow = new FlowRef();
				sourceFlow.flow = FlowDescriptor.create()
					.refId(sid)
					.name(Maps.getString(row, 3))
					.get();
				sourceFlow.flowCategory = Maps.getString(row, 4);
				sourceFlow.flowLocation = Maps.getString(row, 5);

				// flow property
				String sprop = Maps.getString(row, 9);
				if (Strings.notEmpty(sprop)) {
					sourceFlow.property = FlowPropertyDescriptor.create()
						.refId(sprop)
						.name(Maps.getString(row, 10))
						.get();
				}

				// unit
				String sunitID = Maps.getString(row, 13);
				String sunitName = Maps.getString(row, 14);
				if (Strings.notEmpty(sunitID) || Strings.notEmpty(sunitName)) {
					sourceFlow.unit = UnitDescriptor.create()
						.refId(sunitID)
						.name(sunitName)
						.get();
				}

				// status
				String sstatus = Maps.getString(row, 21);
				if (Strings.notEmpty(sstatus)) {
					sourceFlow.status = MappingStatus.fromString(sstatus);
				}
			}

			// target flow
			FlowRef targetFlow = null;
			String tid = Maps.getString(row, 1);
			if (Strings.notEmpty(tid)) {
				targetFlow = new FlowRef();
				targetFlow.flow = FlowDescriptor.create()
					.refId(tid)
					.name(Maps.getString(row, 6))
					.get();
				targetFlow.flowCategory = Maps.getString(row, 7);
				targetFlow.flowLocation = Maps.getString(row, 8);

				// flow property
				String tprop = Maps.getString(row, 11);
				if (Strings.notEmpty(tprop)) {
					targetFlow.property = FlowPropertyDescriptor.create()
						.refId(tprop)
						.name(Maps.getString(row, 12))
						.get();
				}

				// unit
				String tunitID = Maps.getString(row, 15);
				String tunitName = Maps.getString(row, 16);
				if (Strings.notEmpty(tunitID) || Strings.notEmpty(tunitName)) {
					targetFlow.unit = UnitDescriptor.create()
						.refId(tunitID)
						.name(tunitName)
						.get();
				}

				// provider
				String prov = Maps.getString(row, 17);
				if (Strings.notEmpty(prov)) {
					targetFlow.provider = ProcessDescriptor.create()
						.refId(prov)
						.name(Maps.getString(row, 18))
						.get();
					targetFlow.providerCategory = Maps.getString(row, 19);
					targetFlow.providerLocation = Maps.getString(row, 20);
				}

				// status
				String tstatus = Maps.getString(row, 22);
				if (Strings.notEmpty(tstatus)) {
					targetFlow.status = MappingStatus.fromString(tstatus);
				}
			}

			if (sourceFlow != null || targetFlow != null) {
				var factor = Maps.getDouble(row, 2);
				fm.entries.add(new FlowMapEntry(sourceFlow, targetFlow, factor));
			}
		});
		return fm;
	}

	/**
	 * Converts this flow map into the openLCA internal storage format.
	 */
	public MappingFile toMappingFile() {
		var mapping = new MappingFile();
		mapping.name = name;
		updateContentOf(mapping);
		return mapping;
	}

	/**
	 * Updates the content of the given mapping file with the data of this flow
	 * map.
	 */
	public void updateContentOf(MappingFile mapping) {
		if (mapping == null)
			return;
		var content = toCsv(this);
		mapping.content = content.length == 0
			? null
			: BinUtils.gzip(content);
	}

	public static byte[] toCsv(FlowMap fm) {
		if (fm == null)
			return new byte[0];
		var stream = new ByteArrayOutputStream();
		toCsv(fm, stream);
		return stream.toByteArray();
	}

	public static void toCsv(FlowMap fm, File file) {
		if (fm == null || file == null)
			return;
		try (var stream = new FileOutputStream(file)) {
			toCsv(fm, stream);
		} catch (IOException e) {
			throw new RuntimeException("Failed to " +
				"write mapping to file " + file, e);
		}
	}

	public static void toCsv(FlowMap fm, OutputStream stream) {
		if (fm == null || stream == null)
			return;
		Maps.write(stream, fm.entries.stream().map(e -> {
			Object[] row = new Object[23];
			row[2] = e.factor();

			// source flow
			FlowRef s = e.sourceFlow();
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
			FlowRef t = e.targetFlow();
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

		var array = Json.getArray(obj, "mappings");
		if (array != null) {
			for (JsonElement e : array) {
				if (!e.isJsonObject())
					continue;
				var eObj = e.getAsJsonObject();
				var sourceFlow = asFlowRef(Json.getObject(eObj, "from"));
				var targetFlow = asFlowRef(Json.getObject(eObj, "to"));
				var factor = Json.getDouble(eObj, "conversionFactor", 1.0);
				map.entries.add(new FlowMapEntry(sourceFlow, targetFlow, factor));
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
		if (d instanceof FlowDescriptor fd) {
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
