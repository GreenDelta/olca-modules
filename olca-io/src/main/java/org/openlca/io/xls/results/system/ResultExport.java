package org.openlca.io.xls.results.system;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.data_quality.DQCalculationSetup;
import org.openlca.core.math.data_quality.DQResult;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.io.xls.results.CellWriter;
import org.openlca.io.xls.results.InfoSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultExport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(ResultExport.class);
	static final String[] FLOW_HEADER = { "Flow UUID", "Flow", "Category",
			"Sub-category", "Unit" };
	static final String[] PROCESS_HEADER = { "Process UUID", "Process",
			"Location" };
	static final String[] IMPACT_HEADER = { "Impact category UUID",
			"Impact category", "Reference unit" };

	private final File file;
	final CalculationSetup setup;
	final SimpleResult result;
	final EntityCache cache;
	DQResult dqResult;

	private boolean success;
	List<CategorizedDescriptor> processes;
	List<IndexFlow> flows;
	List<ImpactCategoryDescriptor> impacts;
	Workbook workbook;
	CellWriter writer;

	public ResultExport(CalculationSetup setup,
			SimpleResult result, File file, EntityCache cache) {
		this.setup = setup;
		this.result = result;
		this.file = file;
		this.cache = cache;
	}

	public void setDQResult(DQResult dqResult) {
		this.dqResult = dqResult;
	}

	@Override
	public void run() {
		try {
			prepare();
			DQCalculationSetup dqSetup = dqResult != null
					? dqResult.setup
					: null;
			InfoSheet.write(workbook, writer, setup, dqSetup, getType());
			InventorySheet.write(this);
			if (result.hasImpactResults()) {
				ImpactSheet.write(this);
			}
			writeContributionSheets();
			writeUpstreamSheets();
			success = true;
			try (FileOutputStream stream = new FileOutputStream(file)) {
				workbook.write(stream);
			}
		} catch (Exception e) {
			log.error("Error exporting results", e);
			success = false;
		}
	}

	private void writeContributionSheets() {
		if (!(result instanceof ContributionResult))
			return;
		ContributionResult cons = (ContributionResult) result;
		ProcessFlowContributionSheet.write(this, cons);
		if (cons.hasImpactResults()) {
			ProcessImpactContributionSheet.write(this, cons);
			FlowImpactContributionSheet.write(this, cons);
		}
	}

	private void writeUpstreamSheets() {
		if (!(result instanceof FullResult))
			return;
		FullResult r = (FullResult) result;
		ProcessFlowUpstreamSheet.write(this, r);
		if (r.hasImpactResults()) {
			ProcessImpactUpstreamSheet.write(this, r);
		}
	}

	private void prepare() {
		processes = Util.processes(result);
		flows = Util.flows(result, cache);
		impacts = Util.impacts(result);
		// no default flushing (see Excel.cell)!
		workbook = new SXSSFWorkbook(-1);
		writer = new CellWriter(cache, workbook);
	}

	public boolean doneWithSuccess() {
		return success;
	}

	private String getType() {
		if (setup.type == null)
			return "?";
		switch (setup.type) {
		case CONTRIBUTION_ANALYSIS:
			return "Contribution analysis";
		case MONTE_CARLO_SIMULATION:
			return "Monte Carlo simulation";
		case REGIONALIZED_CALCULATION:
			return "Regionalized LCIA calculation";
		case SIMPLE_CALCULATION:
			return "Simple calculation";
		default:
			return "?";
		}
	}

}
