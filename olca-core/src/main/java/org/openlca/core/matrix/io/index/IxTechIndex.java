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
import org.openlca.core.matrix.index.TechIndex;

public record IxTechIndex(List<IxTechItem> items) {

	private static final String NAME = "index_A";

	public static IxTechIndex empty() {
		return new IxTechIndex(Collections.emptyList());
	}

	public static IxTechIndex of(IxTechItem... items) {
		return new IxTechIndex(List.of(items));
	}

	public static IxTechIndex of(TechIndex idx, IxContext ctx) {
		if (idx == null || idx.size() == 0)
			return empty();
		var items = new ArrayList<IxTechItem>(idx.size());
		idx.each((pos, techEntry)
			-> items.add(IxTechItem.of(pos, techEntry, ctx)));
		return new IxTechIndex(items);
	}

	public static IxTechIndex readFromDir(File dir) {
		var proto = IxFormat.PROTO.file(dir, NAME);
		if (proto.exists())
			return readProto(proto);
		var csv = IxFormat.CSV.file(dir, NAME);
		return csv.exists()
			? readCsv(csv)
			: empty();
	}

	public static boolean isPresentInDir(File dir) {
		return IxFormat.PROTO.file(dir, NAME).exists()
			|| IxFormat.CSV.file(dir, NAME).exists();
	}

	public static IxTechIndex readFrom(File file) {
		if (file == null || !file.exists())
			return empty();
		return Csv.isCsv(file)
			? readCsv(file)
			: readProto(file);
	}

	private static IxTechIndex readCsv(File file) {
		var items = new ArrayList<IxTechItem>();
		Csv.eachRowSkipFirst(file,
			row -> items.add(IxTechItem.fromCsv(row)));
		return new IxTechIndex(items);
	}

	private static IxTechIndex readProto(File file) {
		try (var stream = new FileInputStream(file)) {
			var items = new ArrayList<IxTechItem>();
			var proto = IxProto.ProductIndex.parseFrom(stream);
			for (int i = 0; i < proto.getProductCount(); i++) {
				var pi = proto.getProduct(i);
				items.add(IxTechItem.fromProto(pi));
			}
			return new IxTechIndex(items);
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

	public void writeToDir(File dir) {
		writeToDir(dir, IxFormat.PROTO);
	}

	public void writeToDir(File dir, IxFormat format) {
		if (format == IxFormat.CSV) {
			toCsv(IxFormat.CSV.file(dir, NAME));
		} else {
			toProto(IxFormat.PROTO.file(dir, NAME));
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
		var index = IxProto.ProductIndex.newBuilder();
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
