package org.openlca.core.matrix.io.index;

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
import org.openlca.core.library.Library;
import org.openlca.core.matrix.index.EnviIndex;

public record IxEnviIndex(List<IxEnviItem> items) {

	private static final String NAME = "index_B";

	public static IxEnviIndex empty() {
		return new IxEnviIndex(Collections.emptyList());
	}

	public static IxEnviIndex of(IxEnviItem... items) {
		return new IxEnviIndex(List.of(items));
	}

	public static IxEnviIndex of(EnviIndex idx, IxContext ctx) {
		if (idx == null || idx.size() == 0)
			return empty();
		var items = new ArrayList<IxEnviItem>(idx.size());
		idx.each((pos, enviEntry)
			-> items.add(IxEnviItem.of(pos, enviEntry, ctx)));
		return new IxEnviIndex(items);
	}

	public static IxEnviIndex readFrom(Library lib) {
		var proto = IxFormat.PROTO.file(lib, NAME);
		if (proto.exists())
			return readProto(proto);
		var csv = IxFormat.CSV.file(lib, NAME);
		return csv.exists()
			? readCsv(csv)
			: empty();
	}

	public static boolean isPresentIn(Library lib) {
		return IxFormat.PROTO.file(lib, NAME).exists()
			|| IxFormat.CSV.file(lib, NAME).exists();
	}

	public static IxEnviIndex readFrom(File file) {
		if (file == null || !file.exists())
			return empty();
		return Csv.isCsv(file)
			? readCsv(file)
			: readProto(file);
	}

	private static IxEnviIndex readCsv(File file) {
		var items = new ArrayList<IxEnviItem>();
		Csv.eachRowSkipFirst(file,
			row -> items.add(IxEnviItem.fromCsv(row)));
		return new IxEnviIndex(items);
	}

	private static IxEnviIndex readProto(File file) {
		try (var stream = new FileInputStream(file)) {
			var items = new ArrayList<IxEnviItem>();
			var proto = IxProto.ElemFlowIndex.parseFrom(stream);
			for (int i = 0; i < proto.getFlowCount(); i++) {
				var fi = proto.getFlow(i);
				items.add(IxEnviItem.fromProto(fi));
			}
			return new IxEnviIndex(items);
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

	public void writeTo(Library lib) {
		writeTo(lib, IxFormat.PROTO);
	}

	public void writeTo(Library lib, IxFormat format) {
		if (format == IxFormat.CSV) {
			toCsv(IxFormat.CSV.file(lib, NAME));
		} else {
			toProto(IxFormat.PROTO.file(lib, NAME));
		}
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
			throw new RuntimeException("failed to write envi-index to " + file, e);
		}
	}

	public void toProto(File file) {
		var index = IxProto.ElemFlowIndex.newBuilder();
		for (var item : items) {
			index.addFlow(item.toProto());
		}
		try (var stream = new FileOutputStream(file);
				 var buffer = new BufferedOutputStream(stream)) {
			index.build().writeTo(buffer);
		} catch (Exception e) {
			throw new RuntimeException(
				"failed to write envi-index to " + file, e);
		}
	}
}
