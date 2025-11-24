package org.openlca.io.maps;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.openlca.commons.Res;
import org.openlca.commons.Strings;


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

	public static Res<GladFlowMap> readFrom(InputStream stream) {
		try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
				 var parser = CSVFormat.DEFAULT.parse(reader)) {
			var map = new GladFlowMap();
			var first = true;
			for (var row : parser) {
				if (first || row.size() < 2) {
					first = false;
					continue;
				}
				var entryRes = parseEntry(row);
				if (entryRes.isError())
					return entryRes.wrapError(
						"Failed to parse row " + row.getRecordNumber());
				map.entries.add(entryRes.value());
			}
			return Res.ok(map);
		} catch (Exception e) {
			return Res.error("Failed to read GLAD flow map from stream", e);
		}
	}

	private static Res<Entry> parseEntry(CSVRecord row) {
		// source & target flow IDs are required
		var sourceFlowId = get(row, 2);
		if (Strings.isBlank(sourceFlowId))
			return Res.error("Source flow UUID is missing");
		var targetFlowId = get(row, 9);
		if (Strings.isBlank(targetFlowId))
			return Res.error("Target flow UUID is missing");

		// source flow info
		var sourceFlow = new FlowInfo(
				get(row, 0), // SourceListName
				get(row, 1), // SourceFlowName
				sourceFlowId,
				get(row, 3), // SourceFlowContext
				get(row, 4)  // SourceUnit
		);

		// target flow info
		var targetFlow = new FlowInfo(
				get(row, 7),  // TargetListName
				get(row, 8),  // TargetFlowName
				targetFlowId,
				get(row, 10), // TargetFlowContext
				get(row, 11)  // TargetUnit
		);

		// conversion factor
		var factor = get(row, 6);
		if (Strings.isBlank(factor))
			return Res.error("Conversion factor is missing");
		double conversionFactor;
		try {
			conversionFactor = Double.parseDouble(factor);
			if (conversionFactor <= 0)
				return Res.error("Invalid conversion factor: " + conversionFactor);
		} catch (NumberFormatException e) {
			return Res.error("Invalid conversion factor: " + factor);
		}

		return Res.ok(new Entry(
				sourceFlow,
				targetFlow,
				conversionFactor,
				MatchCondition.fromString(get(row, 5)),
				get(row, 12), // Mapper
				get(row, 13), // Verifier
				get(row, 14), // LastUpdated
				get(row, 15), // MemoMapper
				get(row, 16), // MemoVerifier
				get(row, 17), // MemoSource
				get(row, 18)  // MemoTarget
		));
	}

	private static String get(CSVRecord row, int col) {
		if(row == null || col >= row.size())
			return null;
		var v = row.get(col);
		return v != null ? v.trim() : null;
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
			return switch(s.trim()) {
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
	}
}
