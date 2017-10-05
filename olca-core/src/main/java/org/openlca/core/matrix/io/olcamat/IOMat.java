package org.openlca.core.matrix.io.olcamat;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.format.IMatrix;

public final class IOMat {

	private IOMat() {
	}

	public static void writeTechIndex(TechIndexEntry[] entries, File file)
			throws Exception {
		if (entries == null || file == null)
			return;
		List<String> rows = new ArrayList<>(entries.length + 1);
		rows.add(Csv.techIndexHeader());
		for (TechIndexEntry e : entries) {
			rows.add(e.toCsv());
		}
		Csv.writeFile(rows, file);
	}

	public static void writeEnviIndex(EnviIndexEntry[] entries, File file)
			throws Exception {
		if (entries == null || file == null)
			return;
		List<String> rows = new ArrayList<>(entries.length + 1);
		rows.add(Csv.enviIndexHeader());
		for (EnviIndexEntry e : entries) {
			rows.add(e.toCsv());
		}
		Csv.writeFile(rows, file);
	}

	public static TechIndexEntry[] readTechIndex(File file) throws Exception {
		if (file == null)
			return new TechIndexEntry[0];
		List<String> lines = Files.readAllLines(file.toPath());
		TechIndexEntry[] entries = new TechIndexEntry[lines.size() - 1];
		for (int i = 1; i < lines.size(); i++) {
			TechIndexEntry e = TechIndexEntry.fromCsv(lines.get(i));
			entries[e.index] = e;
		}
		return entries;
	}

	public static EnviIndexEntry[] readEnviIndex(File file) throws Exception {
		if (file == null)
			return new EnviIndexEntry[0];
		List<String> lines = Files.readAllLines(file.toPath());
		EnviIndexEntry[] entries = new EnviIndexEntry[lines.size() - 1];
		for (int i = 1; i < lines.size(); i++) {
			EnviIndexEntry e = EnviIndexEntry.fromCsv(lines.get(i));
			entries[e.index] = e;
		}
		return entries;
	}

	public static void writeMatrix(IMatrix m, File file) throws Exception {
		Matrices.writeDenseColumn(m, file);
	}

	public static IMatrix readMatrix(File file) throws Exception {
		return Matrices.readDenseColumn(file);
	}

}
