package org.openlca.core.library;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.matrix.index.EnviIndex;

public record LibEnviIndex(List<LibEnviItem> items) {

	public static LibEnviIndex empty() {
		return new LibEnviIndex(Collections.emptyList());
	}

	public LibEnviIndex of(EnviIndex idx, DbContext ctx) {
		if (idx == null || idx.size() == 0)
			return empty();
		var items = new ArrayList<LibEnviItem>(idx.size());
		idx.each((pos, enviEntry)
			-> items.add(LibEnviItem.of(pos, enviEntry, ctx)));
		return new LibEnviIndex(items);
	}

	public static LibEnviIndex readFrom(Library lib) {
		var proto = new File(lib.folder(), "index_B.bin");
		if (proto.exists())
			return readProto(proto);
		var csv = new File(lib.folder(), "index_B.csv");
		return csv.exists()
			? readCsv(csv)
			: empty();
	}

	public static boolean isPresentIn(Library lib) {
		var proto = new File(lib.folder(), "index_B.bin");
		if (proto.exists())
			return true;
		var csv = new File(lib.folder(), "index_B.csv");
		return csv.exists();
	}

	public static LibEnviIndex readFrom(File file) {
		if (file == null || !file.exists())
			return empty();
		return Csv.isCsv(file)
			? readCsv(file)
			: readProto(file);
	}

	private static LibEnviIndex readCsv(File file) {
		var items = new ArrayList<LibEnviItem>();
		Csv.eachRowSkipFirst(file,
			row -> items.add(LibEnviItem.fromCsv(row)));
		return new LibEnviIndex(items);
	}

	private static LibEnviIndex readProto(File file) {
		try (var stream = new FileInputStream(file)) {
			var items = new ArrayList<LibEnviItem>();
			var proto = Proto.ElemFlowIndex.parseFrom(stream);
			for (int i = 0; i < proto.getFlowCount(); i++) {
				var fi = proto.getFlow(i);
				items.add(LibEnviItem.fromProto(fi));
			}
			return new LibEnviIndex(items);
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to read envi-index from " + file, e);
		}
	}

	public int size() {
		return items.size();
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	public void toCsv(File file) {
		try (var writer = new FileWriter(file, StandardCharsets.UTF_8);
				 var printer = new CSVPrinter(writer, Csv.format())) {

			printer.printRecord(
				"index",
				"flow ID",
				"flow name",
				"flow category",
				"flow unit",
				"flow type",
				"location ID",
				"location name",
				"location code");

			var buffer = new ArrayList<String>(9);
			for (var item : items) {
				item.toCsv(buffer);
				printer.printRecord(buffer);
				buffer.clear();
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to write tech-index to " + file, e);
		}
	}

	public void toProto(File file) {
		var index = Proto.ElemFlowIndex.newBuilder();
		for (var item : items) {
			index.addFlow(item.toProto());
		}
		try (var stream = new FileOutputStream(file);
				 var buffer = new BufferedOutputStream(stream)) {
			index.build().writeTo(buffer);
		} catch (Exception e) {
			throw new RuntimeException(
				"failed to write tech-index to " + file, e);
		}
	}
}
