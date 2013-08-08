package org.openlca.io.xls.results;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.SimulationResult;
import org.openlca.core.results.SimulationStatistics;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

class SimulationExportImpact implements Comparable<SimulationExportImpact> {

	private ImpactCategoryDescriptor category;
	private String unit;
	private String name;

	public SimulationExportImpact(ImpactCategoryDescriptor category) {
		this.category = category;
		this.name = category.getName();
		this.unit = category.getReferenceUnit();
	}

	@Override
	public int compareTo(SimulationExportImpact other) {
		return Strings.compare(name, other.name);
	}

	public void writeRow(HSSFRow aRow, SimulationResult result) {
		Excel.cell(aRow, 0, name);
		Excel.cell(aRow, 1, unit);
		List<Double> results = result.getImpactResults(category.getId());
		SimulationStatistics stat = new SimulationStatistics(results, 100);
		Excel.cell(aRow, 2, stat.getMean());
		Excel.cell(aRow, 3, stat.getStandardDeviation());
		Excel.cell(aRow, 4, stat.getMinimum());
		Excel.cell(aRow, 5, stat.getMaximum());
		Excel.cell(aRow, 6, stat.getMedian());
		Excel.cell(aRow, 7, stat.getPercentileValue(5));
		Excel.cell(aRow, 8, stat.getPercentileValue(95));
	}

	public static String[] getHeaders() {
		return new String[] { "Impact category", "Unit", "Mean",
				"Standard deviation", "Minimum", "Maximum", "Median",
				"5% Percentile", "95% Percentile" };
	}

}
