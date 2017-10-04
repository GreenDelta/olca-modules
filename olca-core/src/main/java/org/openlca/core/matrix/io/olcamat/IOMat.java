package org.openlca.core.matrix.io.olcamat;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.openlca.core.matrix.format.IMatrix;

public final class IOMat {

	private IOMat() {
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
