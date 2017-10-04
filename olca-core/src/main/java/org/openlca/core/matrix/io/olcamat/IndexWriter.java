package org.openlca.core.matrix.io.olcamat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;

class IndexWriter {

	private final TechIndex techIndex;
	private final FlowIndex enviIndex;

	private File folder;
	private Indexer indexer;

	IndexWriter(TechIndex techIndex, FlowIndex enviIndex) {
		this.techIndex = techIndex;
		this.enviIndex = enviIndex;
	}

	void write(IDatabase db, File folder) throws Exception {
		this.folder = folder;
		this.indexer = new Indexer(db);
		writeTechIndex();
		writeEnviIndex();
	}

	private void writeTechIndex() throws Exception {
		List<String> rows = new ArrayList<>(techIndex.size() + 1);
		rows.add(techHeader());
		for (int i = 0; i < techIndex.size(); i++) {
			LongPair idx = techIndex.getProviderAt(i);
			TechIndexEntry e = indexer.getTechEntry(idx);
			e.index = i;
			rows.add(e.toCsv());
		}
		File f = new File(folder, "index_A.csv");
		Csv.writeFile(rows, f);
	}

	private void writeEnviIndex() throws Exception {
		List<String> rows = new ArrayList<>(techIndex.size() + 1);
		rows.add(enviHeader());
		for (int i = 0; i < enviIndex.size(); i++) {
			EnviIndexEntry e = indexer.getEnviEntry(enviIndex.getFlowAt(i));
			e.index = i;
			rows.add(e.toCsv());
		}
		File f = new File(folder, "index_B.csv");
		Csv.writeFile(rows, f);
	}

	private String techHeader() {
		String[] header = {
				"index",
				"process ID",
				"process name",
				"process type",
				"process location",
				"process category",
				"process sub-category",
				"flow ID",
				"flow name",
				"flow type",
				"flow location",
				"flow category",
				"flow sub-category",
				"flow property ID",
				"flow property name",
				"unit ID",
				"unit name" };
		return Csv.toLine(header);
	}

	private String enviHeader() {
		String[] header = {
				"index",
				"flow ID",
				"flow name",
				"flow type",
				"flow location",
				"flow category",
				"flow sub-category",
				"flow property ID",
				"flow property name",
				"unit ID",
				"unit name" };
		return Csv.toLine(header);
	}

}
