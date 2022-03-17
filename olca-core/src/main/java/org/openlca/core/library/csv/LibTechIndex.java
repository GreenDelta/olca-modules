package org.openlca.core.library.csv;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.library.Csv;
import org.openlca.core.library.LibFlow;
import org.openlca.core.library.LibProcess;
import org.openlca.core.library.LibTechItem;
import org.openlca.core.library.Library;
import org.openlca.core.matrix.index.TechIndex;

public final class LibTechIndex {

	private LibTechIndex() {
	}

	public static List<LibTechItem> read(Library library) {
		var file = new File(library.folder, "index_A.csv");
		var entries = new ArrayList<LibTechItem>();
		try (var reader = new FileReader(file, StandardCharsets.UTF_8);
				 var parser = new CSVParser(reader, Csv.format());) {

			boolean isHeader = true;
			boolean sorted = true;
			int i = 0;
			for (var record : parser) {
				if (isHeader) {
					isHeader = false;
					continue;
				}
				var entry = LibTechItem.fromCsv(record);
				sorted = sorted && entry.index() == i;
				entries.add(entry);
				i++;
			}

			if (!sorted) {
				entries.sort((e1, e2) -> Integer.compare(e1.index(), e2.index()));
			}

			return entries;
		} catch (IOException e) {
			throw new RuntimeException("failed to read tech-index from " + file, e);
		}
	}

	public static void write(WriterContext cxt, TechIndex index) {
		if (cxt == null || index == null)
			return;
		var entries = new ArrayList<LibTechItem>(index.size());
		index.each((i, techFlow) -> {
			var process = cxt.toLibProcess(techFlow.provider());
			var flow = cxt.toLibFlow(techFlow.flow());
			entries.add(new LibTechItem(i, process, flow));
		});
		write(cxt, entries);
	}

	public static void write(WriterContext cxt, Iterable<LibTechItem> entries) {
		var file = new File(cxt.library().folder, "index_A.csv");
		try (var writer = new FileWriter(file, StandardCharsets.UTF_8);
				var printer = new CSVPrinter(writer, Csv.format());) {

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
			for (var e : entries) {
				buffer.add(Integer.toString(e.index()));
				var process = Objects.requireNonNullElse(
						e.process(), LibProcess.empty());
				process.toCsv(buffer);
				var flow = Objects.requireNonNullElse(
						e.flow(), LibFlow.empty());
				flow.toCsv(buffer);
				printer.printRecord(buffer);
				buffer.clear();
			}

		} catch (Exception e) {
			throw new RuntimeException("failed to write tech-index to " + file, e);
		}
	}

}
