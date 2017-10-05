package org.openlca.core.matrix.io.olcamat;

import org.openlca.core.model.ProcessType;

/**
 * Contains the meta-data of a row / column of the technology matrix A.
 */
public class TechIndexEntry extends EnviIndexEntry {

	public String processID;
	public String processName;
	public ProcessType processType;
	public String processLocation;
	public String processCategory;
	public String processSubCategory;

	@Override
	public TechIndexEntry clone() {
		TechIndexEntry clone = new TechIndexEntry();
		clone.index = index;
		clone.processID = processID;
		clone.processName = processName;
		clone.processType = processType;
		clone.processLocation = processLocation;
		clone.processCategory = processCategory;
		clone.processSubCategory = processSubCategory;
		clone.flowID = flowID;
		clone.flowName = flowName;
		clone.flowType = flowType;
		clone.flowLocation = flowLocation;
		clone.flowCategory = flowCategory;
		clone.flowSubCategory = flowSubCategory;
		clone.flowPropertyID = flowPropertyID;
		clone.flowPropertyName = flowPropertyName;
		clone.unitID = unitID;
		clone.unitName = unitName;
		return clone;
	}

	@Override
	String toCsv() {
		String[] row = new String[17];
		row[0] = Integer.toString(index);
		row[1] = processID;
		row[2] = processName;
		row[3] = processType != null ? processType.name() : null;
		row[4] = processLocation;
		row[5] = processCategory;
		row[6] = processSubCategory;
		writeFlowInfo(row, 7);
		return Csv.toLine(row);
	}

	static TechIndexEntry fromCsv(String line) throws Exception {
		if (line == null)
			return null;
		String[] row = Csv.readLine(line);
		TechIndexEntry e = new TechIndexEntry();
		e.index = Integer.parseInt(row[0]);
		e.processID = row[1];
		e.processName = row[2];
		e.processType = row[3] != null ? ProcessType.valueOf(row[3]) : null;
		e.processLocation = row[4];
		e.processCategory = row[5];
		e.processSubCategory = row[6];
		readFlowInfo(e, row, 7);
		return e;
	}
}
