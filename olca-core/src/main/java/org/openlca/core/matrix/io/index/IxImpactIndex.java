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
import org.openlca.core.matrix.index.ImpactIndex;

public record IxImpactIndex(List<IxImpactItem> items) {

	private static final String NAME = "index_C";

	public static IxImpactIndex empty() {
		return new IxImpactIndex(Collections.emptyList());
	}

	public static IxImpactIndex of(ImpactIndex idx) {
		if (idx == null || idx.size() == 0)
			return empty();
		var items = new ArrayList<IxImpactItem>(idx.size());
		idx.each((pos, impact)
			-> items.add(IxImpactItem.of(pos, impact)));
		return new IxImpactIndex(items);
	}

	public static IxImpactIndex readFrom(Library lib) {
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

	public static IxImpactIndex readFrom(File file) {
		if (file == null || !file.exists())
			return empty();
		return Csv.isCsv(file)
			? readCsv(file)
			: readProto(file);
	}

	private static IxImpactIndex readCsv(File file) {
		var items = new ArrayList<IxImpactItem>();
		Csv.eachRowSkipFirst(file,
			row -> items.add(IxImpactItem.fromCsv(row)));
		return new IxImpactIndex(items);
	}

	private static IxImpactIndex readProto(File file) {
		try (var stream = new FileInputStream(file)) {
			var items = new ArrayList<IxImpactItem>();
			var proto = IxProto.ImpactIndex.parseFrom(stream);
			for (int i = 0; i < proto.getImpactCount(); i++) {
				var impact = proto.getImpact(i);
				items.add(IxImpactItem.fromProto(impact));
			}
			return new IxImpactIndex(items);
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
		var index = IxProto.ImpactIndex.newBuilder();
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
