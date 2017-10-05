package org.openlca.core.matrix.io.olcamat;

import org.openlca.core.model.FlowType;

/**
 * Contains the meta-data of a row of the intervention matrix A.
 */
public class EnviIndexEntry {

	public int index;

	public String flowID;
	public String flowName;
	public FlowType flowType;
	public String flowLocation;
	public String flowCategory;
	public String flowSubCategory;

	public String flowPropertyID;
	public String flowPropertyName;
	public String unitID;
	public String unitName;

	@Override
	public EnviIndexEntry clone() {
		EnviIndexEntry clone = new EnviIndexEntry();
		clone.index = index;
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

	String toCsv() {
		String[] row = new String[11];
		row[0] = Integer.toString(index);
		writeFlowInfo(row, 1);
		return Csv.toLine(row);
	}

	void writeFlowInfo(String[] row, int offset) {
		row[offset] = flowID;
		row[offset + 1] = flowName;
		row[offset + 2] = flowType != null ? flowType.name() : null;
		row[offset + 3] = flowLocation;
		row[offset + 4] = flowCategory;
		row[offset + 5] = flowSubCategory;
		row[offset + 6] = flowPropertyID;
		row[offset + 7] = flowPropertyName;
		row[offset + 8] = unitID;
		row[offset + 9] = unitName;
	}

	static EnviIndexEntry fromCsv(String line) throws Exception {
		if (line == null)
			return null;
		String[] row = Csv.readLine(line);
		EnviIndexEntry e = new EnviIndexEntry();
		e.index = Integer.parseInt(row[0]);
		readFlowInfo(e, row, 1);
		return e;
	}

	static void readFlowInfo(EnviIndexEntry e, String[] row, int offset) {
		e.flowID = row[offset];
		e.flowName = row[offset + 1];
		String type = row[offset + 2];
		e.flowType = type != null ? FlowType.valueOf(type) : null;
		e.flowLocation = row[offset + 3];
		e.flowCategory = row[offset + 4];
		e.flowSubCategory = row[offset + 5];
		e.flowPropertyID = row[offset + 6];
		e.flowPropertyName = row[offset + 7];
		e.unitID = row[offset + 8];
		e.unitName = row[offset + 9];
	}
}
