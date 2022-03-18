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
import org.openlca.core.matrix.index.ImpactIndex;

public record LibImpactIndex(List<LibImpactItem> items) {

	private static final String NAME = "index_C";

	public static LibImpactIndex empty() {
		return new LibImpactIndex(Collections.emptyList());
	}

	public static LibImpactIndex of(ImpactIndex idx) {
		if (idx == null || idx.size() == 0)
			return empty();
		var items = new ArrayList<LibImpactItem>(idx.size());
		idx.each((pos, impact)
			-> items.add(LibImpactItem.of(pos, impact)));
		return new LibImpactIndex(items);
	}

	public static LibImpactIndex readFrom(Library lib) {
		var proto = IndexFormat.PROTO.file(lib, NAME);
		if (proto.exists())
			return readProto(proto);
		var csv = IndexFormat.CSV.file(lib, NAME);
		return csv.exists()
			? readCsv(csv)
			: empty();
	}

	public static boolean isPresentIn(Library lib) {
		return IndexFormat.PROTO.file(lib, NAME).exists()
			|| IndexFormat.CSV.file(lib, NAME).exists();
	}

	public static LibImpactIndex readFrom(File file) {
		if (file == null || !file.exists())
			return empty();
		return Csv.isCsv(file)
			? readCsv(file)
			: readProto(file);
	}

	private static LibImpactIndex readCsv(File file) {
		var items = new ArrayList<LibImpactItem>();
		Csv.eachRowSkipFirst(file,
			row -> items.add(LibImpactItem.fromCsv(row)));
		return new LibImpactIndex(items);
	}

	private static LibImpactIndex readProto(File file) {
		try (var stream = new FileInputStream(file)) {
			var items = new ArrayList<LibImpactItem>();
			var proto = Proto.ImpactIndex.parseFrom(stream);
			for (int i = 0; i < proto.getImpactCount(); i++) {
				var impact = proto.getImpact(i);
				items.add(LibImpactItem.fromProto(impact));
			}
			return new LibImpactIndex(items);
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to read impact-index from " + file, e);
		}
	}

	public int size() {
		return items.size();
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	public void writeTo(Library lib) {
		writeTo(lib, IndexFormat.PROTO);
	}

	public void writeTo(Library lib, IndexFormat format) {
		if (format == IndexFormat.CSV) {
			toCsv(IndexFormat.CSV.file(lib, NAME));
		} else {
			toProto(IndexFormat.PROTO.file(lib, NAME));
		}
	}

	public void toCsv(File file) {
		try (var writer = new FileWriter(file, StandardCharsets.UTF_8);
				 var printer = new CSVPrinter(writer, Csv.format())) {

			printer.printRecord(
				"index",
				"impact ID",
				"impact name",
				"impact unit");

			var buffer = new ArrayList<String>(9);
			for (var item : items) {
				item.toCsv(buffer);
				printer.printRecord(buffer);
				buffer.clear();
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to write impact-index to " + file, e);
		}
	}

	public void toProto(File file) {
		var index = Proto.ImpactIndex.newBuilder();
		for (var item : items) {
			index.addImpact(item.toProto());
		}
		try (var stream = new FileOutputStream(file);
				 var buffer = new BufferedOutputStream(stream)) {
			index.build().writeTo(buffer);
		} catch (Exception e) {
			throw new RuntimeException(
				"failed to write impact-index to " + file, e);
		}
	}
}
