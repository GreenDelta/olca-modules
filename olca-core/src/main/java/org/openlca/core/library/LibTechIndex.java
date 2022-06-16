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
import org.openlca.core.matrix.index.TechIndex;

public record LibTechIndex(List<LibTechItem> items) {

	private static final String NAME = "index_A";

	public static LibTechIndex empty() {
		return new LibTechIndex(Collections.emptyList());
	}

	public static LibTechIndex of(LibTechItem... items) {
		return new LibTechIndex(List.of(items));
	}

	public static LibTechIndex of(TechIndex idx, DbContext ctx) {
		if (idx == null || idx.size() == 0)
			return empty();
		var items = new ArrayList<LibTechItem>(idx.size());
		idx.each((pos, techEntry)
			-> items.add(LibTechItem.of(pos, techEntry, ctx)));
		return new LibTechIndex(items);
	}

	public static LibTechIndex readFrom(Library lib) {
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

	public static LibTechIndex readFrom(File file) {
		if (file == null || !file.exists())
			return empty();
		return Csv.isCsv(file)
			? readCsv(file)
			: readProto(file);
	}

	private static LibTechIndex readCsv(File file) {
		var items = new ArrayList<LibTechItem>();
		Csv.eachRowSkipFirst(file,
			row -> items.add(LibTechItem.fromCsv(row)));
		return new LibTechIndex(items);
	}

	private static LibTechIndex readProto(File file) {
		try (var stream = new FileInputStream(file)) {
			var items = new ArrayList<LibTechItem>();
			var proto = Proto.ProductIndex.parseFrom(stream);
			for (int i = 0; i < proto.getProductCount(); i++) {
				var pi = proto.getProduct(i);
				items.add(LibTechItem.fromProto(pi));
			}
			return new LibTechIndex(items);
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to read tech-index from " + file, e);
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
				"process ID",
				"process name",
				"process category",
				"process location",
				"flow ID",
				"flow name",
				"flow category",
				"flow unit",
				"flow type");

			var buffer = new ArrayList<String>(10);
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
		var index = Proto.ProductIndex.newBuilder();
		for (var item : items) {
			index.addProduct(item.toProto());
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
