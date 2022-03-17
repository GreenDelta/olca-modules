package org.openlca.core.library;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.util.Exceptions;

public record LibTechIndex(List<LibTechItem> items) {

	public static LibTechIndex empty() {
		return new LibTechIndex(Collections.emptyList());
	}

	public static LibTechIndex of(TechIndex idx, DbContext ctx) {
		if (idx == null || idx.size() == 0)
			return empty();
		var items = new ArrayList<LibTechItem>(idx.size());
		idx.each((pos, techEntry) -> {
			var item = LibTechItem.of(pos, techEntry, ctx);
			items.add(item);
		});
		return new LibTechIndex(items);
	}

	public static LibTechIndex readFrom(Library lib) {
		var proto = new File(lib.folder(), "index_A.bin");
		if (proto.exists())
			return readProto(proto);
		var csv = new File(lib.folder(), "index_A.csv");
		return csv.exists()
			? readCsv(csv)
			: empty();
	}

	public static boolean isPresentIn(Library lib) {
		var proto = new File(lib.folder(), "index_A.bin");
		if (proto.exists())
			return true;
		var csv = new File(lib.folder(), "index_A.csv");
		return csv.exists();
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
		try (var reader = new FileReader(file, StandardCharsets.UTF_8);
				 var parser = new CSVParser(reader, Csv.format())) {
			boolean isHeader = true;
			for (var record : parser) {
				if (isHeader) {
					isHeader = false;
					continue;
				}
				items.add(LibTechItem.fromCsv(record));
			}
			return new LibTechIndex(items);
		} catch (IOException e) {
			throw new RuntimeException("failed to read tech-index from " + file, e);
		}
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
			throw new RuntimeException("failed to read tech-index from " + file, e);
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
			Exceptions.unchecked(
				"failed to write tech-index to " + file, e);
		}
	}
}
