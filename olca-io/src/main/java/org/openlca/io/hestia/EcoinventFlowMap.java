package org.openlca.io.hestia;

import static org.openlca.commons.Strings.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.maps.FlowMap;
import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.io.maps.FlowRef;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;


/// Builds a flow mapping based on the ecoinvent (ei) mapping information in
/// the Hestia glossary. First, the Hestia glossary is downloaded to a folder
/// and the mappings are extracted. For a Hestia term (flow), ei mappings have
/// the following format:
///
/// `{activity name}:{conversion factor}`
///
/// From a given openLCA ecoinvent database, we try to find the corresponding
/// processes for these activity names. For a Hestia activity name, we typically
/// find multiple processes with a corresponding name but different locations
/// in openLCA. When all these processes have the same reference flow, we map
/// that flow to the Hestia term.
public class EcoinventFlowMap {

	private final IDatabase db;
	private final File downloadDir;
	private final HestiaClient client;

	private EcoinventFlowMap(
		IDatabase db, HestiaClient client, File downloadDir) {
		this.db = db;
		this.client = client;
		this.downloadDir = downloadDir;
	}

	public static Res<FlowMap> buildFrom(
		IDatabase db, HestiaClient client, File downloadDir) {
		try {
			return new EcoinventFlowMap(db, client, downloadDir).build();
		} catch (Exception e) {
			return Res.error("Failed to create flowmap", e);
		}
	}

	private Res<FlowMap> build() {

		var procIdx = indexProcesses();
		if (procIdx.isEmpty())
			return Res.ok(FlowMap.empty());

		var res = GlossaryFetch.run(client, downloadDir);
		if (res.isError())
			return res.wrapError("Failed to download glossary");
		var termRes = readTermMappings();
		if (termRes.isError())
			return res.wrapError("Failed to read glossary file(s)");

		var termMappings = termRes.value();
		if (termMappings.isEmpty())
			return Res.ok(FlowMap.empty());

		var flowMap = new FlowMap();
		for (var tm : termMappings) {
			var ps = procIdx.get(tm.activity);
			var flow = flowOf(ps);
			if (flow == null)
				continue;
			var sourceRef = tm.asFlowRef();
			var targetRef = FlowRef.of(flow);
			flowMap.entries.add(
				new FlowMapEntry(sourceRef, targetRef, tm.conversionFactor));
		}
		return Res.ok(flowMap);
	}

	private Res<List<TermMapping>> readTermMappings() {
		var files = downloadDir.listFiles();
		if (files == null || files.length == 0)
			return Res.ok(List.of());

		var mappings = new ArrayList<TermMapping>();
		for (var f : files) {
			var gf = GlossaryFile.readFrom(f);
			if (gf.isError())
				return gf.wrapError("Failed to read glossary file: " + f.getName());
			for (var e : gf.value().entries()) {
				var mapping = TermMapping.tryParse(e);
				if (mapping != null) {
					mappings.add(mapping);
				}
			}
		}
		return Res.ok(mappings);
	}

	private Map<String, List<RootDescriptor>> indexProcesses() {
		var map = new HashMap<String, List<RootDescriptor>>();
		for (var d : db.getDescriptors(Process.class)) {
			if (Strings.isBlank(d.name))
				continue;
			var parts = d.name.split("\\|");
			if (parts.length < 2)
				continue;
			var process = parts[0].trim();
			var flow = parts[1].trim();
			var key = (flow + ", " + process).toLowerCase();
			map.computeIfAbsent(key, $ -> new ArrayList<>()).add(d);
		}
		return map;
	}

	private Flow flowOf(List<RootDescriptor> ps) {
		if (ps == null || ps.isEmpty())
			return null;
		Flow flow = null;
		for (var p : ps) {
			var process = db.get(Process.class, p.id);
			if (process == null)
				return null;
			var qRef = process.quantitativeReference;
			if (qRef == null)
				return null;
			if (flow == null) {
				flow = qRef.flow;
				continue;
			}
			if (!Objects.equals(flow, qRef.flow))
				return null;
		}
		return flow;
	}

	private record TermMapping(
		String id, String name, String activity, double conversionFactor) {

		static TermMapping tryParse(GlossaryFile.Entry e) {
			if (e == null)
				return null;

			var id = e.getId();
			var name = e.getName();
			var eim = e.getEcoinventMapping();

			if (isBlank(id) || isBlank(name) || isBlank(eim))
				return null;
			var colonIdx = eim.lastIndexOf(':');
			if (colonIdx < 0)
				return new TermMapping(id, name, eim, 1.0);

			var activity = eim.substring(0, colonIdx).trim();
			var factor = eim.substring(colonIdx + 1).trim();
			if (isBlank(activity) || isBlank(factor))
				return null;

			try {
				return new TermMapping(
					id, name, activity, Double.parseDouble(factor));
			} catch (Exception ex) {
				return null;
			}
		}

		FlowRef asFlowRef() {
			var ref = new FlowRef();
			var d = new FlowDescriptor();
			d.refId = id;
			d.name = name;
			ref.flow = d;
			return ref;
		}

	}
}
