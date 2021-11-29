package org.openlca.io.xls.results;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.SimulationResult;
import org.openlca.core.results.Statistics;
import org.openlca.io.xls.Excel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Exports a simulation result to Excel. */
public class SimulationResultExport {

	private static final String[] FLOW_HEADER = {
			"Flow UUID",
			"Flow",
			"Category",
			"Sub-category",
			"Unit"
	};

	private static final String[] IMPACT_HEADER = {
			"Impact category UUID",
			"Impact category",
			"Reference unit"
	};

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final CalculationSetup setup;
	private final SimulationResult result;
	private final EntityCache cache;

	private int row = 0;
	private CellWriter writer;
	private boolean useStreaming = false;

	public SimulationResultExport(CalculationSetup setup,
			SimulationResult result, EntityCache cache) {
		this.setup = setup;
		this.result = result;
		this.cache = cache;
	}

	/**
	 * Runs the result export. The given file should be an xlsx file.
	 */
	public void run(File file) throws Exception {
		useStreaming = result.getNumberOfRuns() > 150;
		log.trace("create workbook, using streaming: {}", useStreaming);
		var wb = useStreaming
				? new SXSSFWorkbook(-1)
				: new XSSFWorkbook();
		writer = new CellWriter(cache, wb);

		// total LCI & LCIA results
		InfoSheet.write(wb, writer, setup, null, "Simulation result");
		writeInventorySheet(wb);
		if (result.hasImpacts()) {
			writeImpactSheet(wb);
		}

		// pinned contributions
		int pcount = 0;
		for (TechFlow pp : result.getPinnedProducts()) {
			pcount++;
			Sheet psheet = wb.createSheet("Contributions " + pcount);
			fillContributions(pp, psheet);
			flushSheet(psheet);
		}

		try (FileOutputStream fos = new FileOutputStream(file)) {
			wb.write(fos);
		}
		if (wb instanceof SXSSFWorkbook) {
			((SXSSFWorkbook) wb).dispose();
		}
		log.trace("result written to file {}", file);
	}

	private void writeImpactSheet(Workbook wb) {
		Sheet sheet = wb.createSheet("Impact Assessment");
		Excel.trackSize(sheet, 0, IMPACT_HEADER.length + 6);
		row = 0;

		row++;
		writer.headerRow(sheet, row, 1, IMPACT_HEADER);
		int nextCol = IMPACT_HEADER.length + 1;
		writeValueHeaders(sheet, row, nextCol);
		row++;

		for (ImpactDescriptor impact : result.getImpacts()) {
			writer.impactRow(sheet, row, 1, impact);
			double[] values = result.getAll(impact);
			writeValues(sheet, row, IMPACT_HEADER.length + 1, values);
			row++;
		}
		Excel.autoSize(sheet, 0, IMPACT_HEADER.length + 6);
	}

	private void writeInventorySheet(Workbook wb) {
		Sheet sheet = wb.createSheet("Inventory");
		Excel.trackSize(sheet, 0, FLOW_HEADER.length + 6);
		row = 0;
		List<EnviFlow> flows = result.getFlows();
		writeInventorySection(flows, true, sheet);
		writeInventorySection(flows, false, sheet);
		Excel.autoSize(sheet, 0, FLOW_HEADER.length + 6);
		flushSheet(sheet);
	}

	private void writeInventorySection(List<EnviFlow> flows,
			boolean forInputs, Sheet sheet) {
		row++;
		String section = forInputs ? "Inputs" : "Outputs";
		writer.cell(sheet, row, 1, section, true);
		row++;
		writer.headerRow(sheet, row, 1, FLOW_HEADER);
		int nextCol = FLOW_HEADER.length + 1;
		writeValueHeaders(sheet, row, nextCol);
		row++;

		for (EnviFlow flow : flows) {
			if (flow.isInput() != forInputs)
				continue;
			writer.flowRow(sheet, row, 1, flow);
			double[] values = result.getAll(flow);
			writeValues(sheet, row, FLOW_HEADER.length + 1, values);
			row++;
		}
	}

	private void fillContributions(TechFlow pp, Sheet sheet) {
		row = 0;

		String label = "Contributions of: ";
		if (pp.provider() != null) {
			label += pp.provider().name;
			if (pp.provider() instanceof ProcessDescriptor) {
				ProcessDescriptor p = (ProcessDescriptor) pp.provider();
				if (p.location != null) {
					Location loc = cache.get(Location.class, p.location);
					if (loc != null) {
						label += " - " + loc.code;
					}
				}
			}
		}
		if (pp.flow() != null) {
			label += " | " + pp.flow().name;
			if (pp.flow().location != null) {
				Location loc = cache.get(Location.class, pp.flow().location);
				if (loc != null) {
					label += " - " + loc.code;
				}
			}
		}

		writer.headerRow(sheet, row, 1, label);
		row++;
		row++;

		if (result.hasImpacts()) {

			writer.headerRow(sheet, row++, 1, "Direct LCIA contributions");
			writer.headerRow(sheet, row, 1, IMPACT_HEADER);
			int valCol = IMPACT_HEADER.length + 1;
			writeValueHeaders(sheet, row++, valCol);
			for (ImpactDescriptor impact : result.getImpacts()) {
				writer.impactRow(sheet, row, 1, impact);
				double[] values = result.getAllDirect(pp, impact);
				writeValues(sheet, row, IMPACT_HEADER.length + 1, values);
				row++;
			}
			row++;

			writer.headerRow(sheet, row++, 1, "Upstream LCIA contributions");
			writer.headerRow(sheet, row, 1, IMPACT_HEADER);
			writeValueHeaders(sheet, row++, valCol);
			for (ImpactDescriptor impact : result.getImpacts()) {
				writer.impactRow(sheet, row, 1, impact);
				double[] values = result.getAllUpstream(pp, impact);
				writeValues(sheet, row, IMPACT_HEADER.length + 1, values);
				row++;
			}
			row++;
		}

		List<EnviFlow> flows = result.getFlows();

		writer.headerRow(sheet, row++, 1, "Direct LCI contributions - Inputs");
		writeFlowContributions(flows, pp, true, result::getAllDirect, sheet);

		writer.headerRow(sheet, row++, 1, "Direct LCI contributions - Outputs");
		writeFlowContributions(flows, pp, false, result::getAllDirect, sheet);

		writer.headerRow(sheet, row++, 1,
				"Upstream LCI contributions - Inputs");
		writeFlowContributions(flows, pp, true, result::getAllUpstream, sheet);

		writer.headerRow(sheet, row++, 1,
				"Upstream LCI contributions - Outputs");
		writeFlowContributions(flows, pp, false, result::getAllUpstream, sheet);

	}

	private void writeFlowContributions(
			List<EnviFlow> flows,
			TechFlow pp,
			boolean forInputs,
			BiFunction<TechFlow, EnviFlow, double[]> fn,
			Sheet sheet) {
		writer.headerRow(sheet, row, 1, FLOW_HEADER);
		int valCol = FLOW_HEADER.length + 1;
		writeValueHeaders(sheet, row++, valCol);
		for (EnviFlow flow : flows) {
			if (flow.isInput() != forInputs)
				continue;
			writer.flowRow(sheet, row, 1, flow);
			double[] values = fn.apply(pp, flow);
			writeValues(sheet, row, valCol, values);
			row++;
		}
		row++;
	}

	private void writeValueHeaders(Sheet sheet, int row, int startCol) {
		String[] vals = { "Mean", "Standard deviation", "Minimum", "Maximum",
				"Median", "5% Percentile", "95% Percentile" };
		for (int i = 0; i < vals.length; i++)
			writer.cell(sheet, row, startCol + i, vals[i], true);
		int nextCol = startCol + vals.length;
		for (int i = 0; i < result.getNumberOfRuns(); i++)
			writer.cell(sheet, row, nextCol++, "Run " + (i + 1), true);
	}

	private void writeValues(Sheet sheet, int row, int startCol,
			double[] values) {
		if (values == null)
			return;
		int col = startCol;
		Statistics stats = Statistics.of(values);
		Excel.cell(sheet, row, col++, stats.mean);
		Excel.cell(sheet, row, col++, stats.standardDeviation);
		Excel.cell(sheet, row, col++, stats.min);
		Excel.cell(sheet, row, col++, stats.max);
		Excel.cell(sheet, row, col++, stats.median);
		Excel.cell(sheet, row, col++, stats.getPercentileValue(5));
		Excel.cell(sheet, row, col++, stats.getPercentileValue(95));
		for (double value : values) {
			Excel.cell(sheet, row, col++, value);
		}
	}

	private void flushSheet(Sheet sheet) {
		if (!useStreaming)
			return;
		if (!(sheet instanceof SXSSFSheet))
			return;
		SXSSFSheet s = (SXSSFSheet) sheet;
		try {
			log.trace("flush rows of sheet {}", sheet.getSheetName());
			s.flushRows();
		} catch (Exception e) {
			log.error("failed to flush rows of streamed sheet", e);
		}
	}

}
