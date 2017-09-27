package org.openlca.core.matrix.io.olcamat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

class IndexWriter {

	private final char separator = ',';

	private final TechIndex techIndex;
	private final FlowIndex enviIndex;

	private File folder;

	private Map<Long, ProcessDescriptor> processes;
	private Map<Long, FlowDescriptor> flows;
	private Map<Long, Location> locations;
	private Map<Long, FlowProperty> flowProperties;

	IndexWriter(TechIndex techIndex, FlowIndex enviIndex) {
		this.techIndex = techIndex;
		this.enviIndex = enviIndex;
	}

	void write(IDatabase db, File folder) throws Exception {
		this.folder = folder;
		loadData(db);
		writeTechIndex();
		writeEnviIndex();
	}

	private void loadData(IDatabase db) {
		processes = descriptors(new ProcessDao(db));
		flows = descriptors(new FlowDao(db));
		locations = all(new LocationDao(db));
		flowProperties = all(new FlowPropertyDao(db));
	}

	private <T extends BaseDescriptor> Map<Long, T> descriptors(RootEntityDao<?, T> dao) {
		Map<Long, T> map = new HashMap<>();
		for (T d : dao.getDescriptors()) {
			map.put(d.getId(), d);
		}
		return map;
	}

	private <T extends RootEntity> Map<Long, T> all(RootEntityDao<T, ?> dao) {
		Map<Long, T> map = new HashMap<>();
		for (T d : dao.getAll()) {
			map.put(d.getId(), d);
		}
		return map;
	}

	private void writeTechIndex() throws Exception {
		List<String> rows = new ArrayList<>(techIndex.size() + 1);
		rows.add(techHeader());
		for (int i = 0; i < techIndex.size(); i++) {
			String[] row = new String[13];
			row[0] = Integer.toString(i);
			LongPair idx = techIndex.getProviderAt(i);
			writeProcessInfo(idx.getFirst(), row);
			writeFlowInfo(idx.getSecond(), row, 5);
			rows.add(toRow(row));
		}
		File f = new File(folder, "index_A.csv");
		writeFile(rows, f);
	}

	private void writeEnviIndex() throws Exception {
		List<String> rows = new ArrayList<>(techIndex.size() + 1);
		rows.add(enviHeader());
		for (int i = 0; i < enviIndex.size(); i++) {
			String[] row = new String[9];
			row[0] = Integer.toString(i);
			writeFlowInfo(enviIndex.getFlowAt(i), row, 1);
			rows.add(toRow(row));
		}
		File f = new File(folder, "index_B.csv");
		writeFile(rows, f);
	}

	private void writeProcessInfo(long processID, String[] row) {
		ProcessDescriptor p = processes.get(processID);
		if (p == null)
			return;
		row[1] = p.getRefId();
		row[2] = p.getName();
		if (p.getProcessType() != null) {
			row[3] = p.getProcessType().name();
		}
		Location l = locations.get(p.getLocation());
		if (l != null) {
			row[4] = l.getCode();
		}
	}

	private void writeFlowInfo(long flowID, String[] row, int offset) {
		FlowDescriptor f = flows.get(flowID);
		if (f == null)
			return;
		row[offset] = f.getRefId();
		row[offset + 1] = f.getName();
		if (f.getFlowType() != null) {
			row[offset + 2] = f.getFlowType().name();
		}
		Location location = locations.get(f.getLocation());
		if (location != null) {
			row[offset + 3] = location.getCode();
		}
		FlowProperty fp = flowProperties.get(f.getRefFlowPropertyId());
		if (fp == null)
			return;
		row[offset + 4] = fp.getRefId();
		row[offset + 5] = fp.getName();
		UnitGroup ug = fp.getUnitGroup();
		if (ug == null || ug.getReferenceUnit() == null)
			return;
		Unit u = ug.getReferenceUnit();
		row[offset + 6] = u.getRefId();
		row[offset + 7] = u.getName();
	}

	private String techHeader() {
		String[] header = {
				"index",
				"process ID",
				"process name",
				"process type",
				"process location",
				"flow ID",
				"flow name",
				"flow type",
				"flow location",
				"flow property ID",
				"flow property name",
				"unit ID",
				"unit name" };
		return toRow(header);
	}

	private String enviHeader() {
		String[] header = {
				"index",
				"flow ID",
				"flow name",
				"flow type",
				"flow location",
				"flow property ID",
				"flow property name",
				"unit ID",
				"unit name" };
		return toRow(header);
	}

	private String toRow(String[] entries) {
		if (entries == null)
			return "";
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < entries.length; i++) {
			String e = entries[i];
			boolean last = i == entries.length - 1;
			if (e == null) {
				if (!last)
					b.append(separator);
				continue;
			}
			e = e.trim().replace('"', '\'');
			if (e.indexOf(separator) >= 0) {
				b.append('"').append(e).append('"');
			} else {
				b.append(e);
			}
			if (!last)
				b.append(separator);
		}
		return b.toString();
	}

	private void writeFile(List<String> lines, File file) throws Exception {
		try (FileOutputStream fos = new FileOutputStream(file);
				OutputStreamWriter writer = new OutputStreamWriter(fos, "utf-8");
				BufferedWriter buffer = new BufferedWriter(writer)) {
			for (String line : lines) {
				buffer.write(line);
				buffer.newLine();
			}
		}
	}
}
