package org.openlca.io.xls.results;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.openlca.core.database.EntityCache;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.results.SimulationResult;
import org.openlca.core.results.SimulationStatistics;
import org.openlca.io.CategoryPair;
import org.openlca.io.DisplayValues;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

class SimulationExportFlow implements Comparable<SimulationExportFlow> {

	private FlowDescriptor flow;
	private boolean input;
	private String name;
	private CategoryPair category;
	private String unit;

	public SimulationExportFlow(FlowDescriptor flow, boolean input,
			EntityCache cache) {
		this.flow = flow;
		this.input = input;
		this.name = flow.getName();
		if (flow.getCategory() == null)
			category = new CategoryPair(null);
		else {
			Category cat = cache.get(Category.class, flow.getCategory());
			this.category = new CategoryPair(cat);
		}
		unit = DisplayValues.referenceUnit(flow, cache);
	}

	public boolean isInput() {
		return input;
	}

	@Override
	public int compareTo(SimulationExportFlow other) {
		int c = this.category.compareTo(other.category);
		if (c != 0)
			return c;
		return Strings.compare(name, other.name);
	}

	public void writeRow(HSSFRow aRow, SimulationResult result) {
		Excel.cell(aRow, 0, category.getCategory());
		Excel.cell(aRow, 1, category.getSubCategory());
		Excel.cell(aRow, 2, name);
		Excel.cell(aRow, 3, unit);
		List<Double> results = result.getFlowResults(flow.getId());
		SimulationStatistics stat = new SimulationStatistics(results, 100);
		Excel.cell(aRow, 4, stat.getMean());
		Excel.cell(aRow, 5, stat.getStandardDeviation());
		Excel.cell(aRow, 6, stat.getMinimum());
		Excel.cell(aRow, 7, stat.getMaximum());
		Excel.cell(aRow, 8, stat.getMedian());
		Excel.cell(aRow, 9, stat.getPercentileValue(5));
		Excel.cell(aRow, 10, stat.getPercentileValue(95));
	}

	public static String[] getHeaders() {
		return new String[] { "Category", "Sub-category", "Flow", "Unit",
				"Mean", "Standard deviation", "Minimum", "Maximum", "Median",
				"5% Percentile", "95% Percentile" };
	}
}
