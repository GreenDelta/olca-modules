package org.openlca.io.xls.results.system;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.math.data_quality.DQCalculationSetup;
import org.openlca.core.math.data_quality.DQResult;
import org.openlca.core.model.NwSet;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.io.xls.results.CellWriter;
import org.openlca.io.xls.results.InfoSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultExport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(ResultExport.class);
	static final String[] FLOW_HEADER = { "Flow UUID", "Flow", "Category", "Sub-category", "Unit" };
	static final String[] PROCESS_HEADER = { "Process UUID", "Process", "Location" };
	static final String[] IMPACT_HEADER = { "Impact category UUID", "Impact category", "Reference unit" };
	static final String[] IMPACT_NW_HEADER = { "Normalized", "Weighted", "Single score unit" };

	private final File file;
	final CalculationSetup setup;
	final SimpleResult result;
	final EntityCache cache;
	DQResult dqResult;

	private boolean success;
	NwSet nwSet;
	SXSSFWorkbook workbook;
	CellWriter writer;

	public ResultExport(CalculationSetup setup,
			SimpleResult result, File file, EntityCache cache) {
		this.setup = setup;
		this.result = result;
		this.file = file;
		this.cache = cache;
		if (setup.nwSet() != null) {
			this.nwSet = cache.get(NwSet.class, setup.nwSet().id);
		}
	}

	public void setDQResult(DQResult dqResult) {
		this.dqResult = dqResult;
	}

	@Override
	public void run() {
		try {
			workbook = new SXSSFWorkbook(-1);
			writer = new CellWriter(cache, workbook);
			DQCalculationSetup dqSetup = dqResult != null
					? dqResult.setup
					: null;
			InfoSheet.write(workbook, writer, setup, dqSetup, getType());
			InventorySheet.write(this);
			if (result.hasImpacts()) {
				ImpactSheet.write(this);
			}
			writeContributionSheets();
			writeUpstreamSheets();
			success = true;
			try (FileOutputStream stream = new FileOutputStream(file)) {
				workbook.write(stream);
			}
			workbook.dispose();
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
		if (cons.hasImpacts()) {
			ProcessImpactContributionSheet.write(this, cons);
			FlowImpactContributionSheet.write(this, cons);
		}
	}

	private void writeUpstreamSheets() {
		if (!(result instanceof FullResult))
			return;
		FullResult r = (FullResult) result;
		ProcessFlowUpstreamSheet.write(this, r);
		if (r.hasImpacts()) {
			ProcessImpactUpstreamSheet.write(this, r);
		}
	}

	public boolean doneWithSuccess() {
		return success;
	}

	private String getType() {
		if (result instanceof FullResult)
			return "Analysis result";
		if (result instanceof ContributionResult)
			return "Contribution result";
		return "Simple result";
	}
}
