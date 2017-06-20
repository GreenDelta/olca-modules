package org.openlca.io.xls.results.system;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.data_quality.DQCalculationSetup;
import org.openlca.core.math.data_quality.DQResult;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.ContributionResultProvider;
import org.openlca.io.xls.results.CellWriter;
import org.openlca.io.xls.results.InfoSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultExport implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(ResultExport.class);
	static final String[] FLOW_HEADER = { "Flow UUID", "Flow", "Category", "Sub-category", "Unit" };
	static final String[] PROCESS_HEADER = { "Process UUID", "Process", "Location" };
	static final String[] IMPACT_HEADER = { "Impact category UUID", "Impact category", "Reference unit" };

	private final File file;
	final CalculationSetup setup;
	final ContributionResultProvider<?> result;
	final DQResult dqResult;
	final String type;

	private boolean success;
	List<ProcessDescriptor> processes;
	List<FlowDescriptor> flows;
	List<ImpactCategoryDescriptor> impacts;
	Workbook workbook;
	CellWriter writer;

	public ResultExport(CalculationSetup setup, ContributionResultProvider<?> result, DQResult dqResult, String type,
			File file) {
		this.setup = setup;
		this.result = result;
		this.dqResult = dqResult;
		this.type = type;
		this.file = file;
	}

	public void run() {
		try {
			prepare();
			DQCalculationSetup dqSetup = dqResult != null ? dqResult.setup : null;
			InfoSheet.write(workbook, writer, setup, dqSetup, type);
			InventorySheet.write(this);
			if (result.hasImpactResults()) {
				ImpactSheet.write(this);
			}
			ProcessFlowContributionSheet.write(this);
			if (result.hasCostResults()) {
				ProcessImpactContributionSheet.write(this);
				FlowImpactContributionSheet.write(this);
			}
			success = true;
			try (FileOutputStream stream = new FileOutputStream(file)) {
				workbook.write(stream);
			}
		} catch (Exception e) {
			log.error("Error exporting results", e);
			success = false;
		}
	}

	private void prepare() {
		processes = Prepare.processes(result);
		flows = Prepare.flows(result);
		impacts = Prepare.impacts(result);
		// no default flushing (see Excel.cell)!
		workbook = new SXSSFWorkbook(-1);
		writer = new CellWriter(result.cache, workbook);
	}

	public boolean doneWithSuccess() {
		return success;
	}

}
