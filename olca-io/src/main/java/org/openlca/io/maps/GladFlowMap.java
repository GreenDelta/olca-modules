package org.openlca.io.maps;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.openlca.core.io.maps.FlowMap;
import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.io.maps.FlowRef;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;
import org.openlca.util.KeyGen;


/// Implements the [EPA/GLAD flow mapping format](https://github.com/UNEP-Economy-Division/GLAD-ElementaryFlowResources).
public class GladFlowMap {

	private final List<Entry> entries = new ArrayList<>();

	public static Res<GladFlowMap> readFrom(File file) {
		try (var stream = new FileInputStream(file)) {
			return readFrom(stream);
		} catch (Exception e) {
			return Res.error("Failed to read GLAD flow map from file: " + file, e);
		}
	}

	/// Returns `true` when the given file is probably a GLAD flow mapping file.
	/// We assume that this is true, if it can be read as a CSV file and has at
	/// least these two column headers: `SourceFlowName` and `TargetFlowName`
	public static boolean isMappingFile(File file) {
		if (file == null || !file.exists() || !file.isFile())
			return false;
		try (var stream = new FileInputStream(file);
				 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
				 var csv = CSVFormat.DEFAULT.parse(reader)) {

			var iter = csv.iterator();
			if (!iter.hasNext())
				return false;

			var header = iter.next();
			boolean hasSource = false;
			boolean hasTarget = false;
			for (int i = 0; i < header.size(); i++) {
				var v = header.get(i);
				if (v == null)
					continue;
				var field = v.trim().toLowerCase();
				if (field.equals("sourceflowname")) {
					hasSource = true;
				} else if (field.equals("targetflowname")) {
					hasTarget = true;
				}
				if (hasSource && hasTarget) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public static Res<GladFlowMap> readFrom(InputStream stream) {
		try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
				 var csv = CSVFormat.DEFAULT.parse(reader)) {
			var parser = new Parser(csv);
			return parser.parse();
		} catch (Exception e) {
			return Res.error("Failed to read GLAD flow map from stream", e);
		}
	}

	public int size() {
		return entries.size();
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public List<Entry> entries() {
		return entries;
	}

	/// Converts this GLAD flow map to an openLCA flow map.
	public FlowMap asFlowMap() {
		var flowMap = new FlowMap();
		var from = new ListInfo();
		var to = new ListInfo();

		for (var e : entries) {

			if (e.sourceFlow == null || e.targetFlow == null)
				continue;

			// source flow
			var source = new FlowRef();
			var sourceInfo = e.sourceFlow();
			source.flow = FlowDescriptor.create()
				.refId(sourceInfo.flowId())
				.name(sourceInfo.flowName())
				.get();
			source.flowCategory = sourceInfo.flowContext();
			if (Strings.isNotBlank(sourceInfo.flowUnit)) {
				source.unit = UnitDescriptor.create()
					.name(sourceInfo.flowUnit)
					.get();
			}
			from.add(sourceInfo.flowList);

			// target flow
			var target = new FlowRef();
			var targetInfo = e.targetFlow();
			target.flow = FlowDescriptor.create()
				.refId(targetInfo.flowId())
				.name(targetInfo.flowName())
				.get();
			target.flowCategory = targetInfo.flowContext();
			to.add(targetInfo.flowList);

			flowMap.entries.add(new FlowMapEntry(
				source, target, e.conversionFactor()
			));
		}

		flowMap.name = from + " -> " + to;
		flowMap.refId = KeyGen.get(flowMap.name);
		return flowMap;
	}

	private static class ListInfo {

		private final Set<String> names = new HashSet<>();

		void add(String s) {
			if (Strings.isNotBlank(s)) {
				names.add(s.trim());
			}
		}

		@Override
		public String toString() {
			if (names.isEmpty())
				return "Unknown";
			return names.size() == 1
				? names.iterator().next()
				: "(" + String.join(", ", names) + ")";
		}
	}

	private static class Parser {

		private final Iterator<CSVRecord> csv;
		private final ColumnLayout layout;

		Parser(CSVParser parser) {
			this.csv = parser.iterator();
			this.layout = this.csv.hasNext()
				? ColumnLayout.parse(csv.next())
				: new ColumnLayout();
		}

		Res<GladFlowMap> parse() {
			var map = new GladFlowMap();
			while (csv.hasNext()) {
				var row = csv.next();
				if (row.size() < 2)
					continue;
				var entry = parseEntry(row);
				if (entry.isError())
					return entry.wrapError(
						"Failed to parse row " + row.getRecordNumber());
				map.entries.add(entry.value());
			}
			return Res.ok(map);
		}

		private Res<Entry> parseEntry(CSVRecord row) {

			// source flow info
			var sourceFlow = new FlowInfo(
				get(row, layout.sourceListName),
				get(row, layout.sourceFlowName),
				get(row, layout.sourceFlowUUID),
				get(row, layout.sourceFlowContext),
				get(row, layout.sourceUnit)
			);

			// target flow info
			var targetFlow = new FlowInfo(
				get(row, layout.targetListName),
				get(row, layout.targetFlowName),
				get(row, layout.targetFlowUUID),
				get(row, layout.targetFlowContext),
				get(row, layout.targetUnit)
			);

			// conversion factor
			double conversionFactor = 1.0;
			var factor = get(row, layout.conversionFactor);
			if (Strings.isNotBlank(factor)) {
				try {
					conversionFactor = Double.parseDouble(factor);
				} catch (NumberFormatException e) {
					return Res.error("Invalid conversion factor: " + factor);
				}
			}

			return Res.ok(new Entry(
				sourceFlow,
				targetFlow,
				conversionFactor,
				MatchCondition.fromString(get(row, layout.matchCondition)),
				get(row, layout.mapper),
				get(row, layout.verifier),
				get(row, layout.lastUpdated),
				get(row, layout.memoMapper),
				get(row, layout.memoVerifier),
				get(row, layout.memoSource),
				get(row, layout.memoTarget)
			));
		}

		private static String get(CSVRecord row, int col) {
			if (row == null || col >= row.size())
				return null;
			var v = row.get(col);
			return v != null ? v.trim() : null;
		}
	}

	public enum MatchCondition {
		SAME("="),
		BROADER(">"),
		NARROWER("<"),
		PROXY("~");

		final String symbol;

		MatchCondition(String symbol) {
			this.symbol = symbol;
		}

		public static MatchCondition fromString(String s) {
			if (Strings.isBlank(s))
				return SAME;
			return switch (s.trim()) {
				case ">" -> BROADER;
				case "<" -> NARROWER;
				case "~" -> PROXY;
				default -> SAME;
			};
		}
	}

	public record Entry(
		FlowInfo sourceFlow,
		FlowInfo targetFlow,
		double conversionFactor,
		MatchCondition matchCondition,
		String mapper,
		String verifier,
		String lastUpdated,
		String memoMapper,
		String memoVerifier,
		String memoSource,
		String memoTarget
	) {
	}

	public record FlowInfo(
		String flowList,
		String flowName,
		String flowId,
		String flowContext,
		String flowUnit
	) {

		@Override
		public String flowId() {
			return Strings.isBlank(flowId)
				? KeyGen.get(flowName, flowContext, flowUnit)
				: flowId;
		}
	}

	private static class ColumnLayout {

		int sourceListName = 0;
		int sourceFlowName = 1;
		int sourceFlowUUID = 2;
		int sourceFlowContext = 3;
		int sourceUnit = 4;
		int matchCondition = 5;
		int conversionFactor = 6;
		int targetListName = 7;
		int targetFlowName = 8;
		int targetFlowUUID = 9;
		int targetFlowContext = 10;
		int targetUnit = 11;
		int mapper = 12;
		int verifier = 13;
		int lastUpdated = 14;
		int memoMapper = 15;
		int memoVerifier = 16;
		int memoSource = 17;
		int memoTarget = 18;

		static ColumnLayout parse(CSVRecord header) {
			var layout = new ColumnLayout();
			for (int i = 0; i < header.size(); i++) {
				var v = header.get(i);
				if (v == null)
					continue;
				switch (v.trim().toLowerCase()) {
					case "sourcelistname" -> layout.sourceListName = i;
					case "sourceflowname" -> layout.sourceFlowName = i;
					case "sourceflowuuid" -> layout.sourceFlowUUID = i;
					case "sourceflowcontext" -> layout.sourceFlowContext = i;
					case "sourceunit" -> layout.sourceUnit = i;
					case "matchcondition" -> layout.matchCondition = i;
					case "conversionfactor" -> layout.conversionFactor = i;
					case "targetlistname" -> layout.targetListName = i;
					case "targetflowname" -> layout.targetFlowName = i;
					case "targetflowuuid" -> layout.targetFlowUUID = i;
					case "targetflowcontext" -> layout.targetFlowContext = i;
					case "targetunit" -> layout.targetUnit = i;
					case "mapper" -> layout.mapper = i;
					case "verifier" -> layout.verifier = i;
					case "lastupdated" -> layout.lastUpdated = i;
					case "memomapper" -> layout.memoMapper = i;
					case "memoverifier" -> layout.memoVerifier = i;
					case "memosource" -> layout.memoSource = i;
					case "memotarget" -> layout.memoTarget = i;
				}
			}
			return layout;
		}

	}
}
