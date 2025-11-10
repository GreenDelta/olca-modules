package org.openlca.io.xls.results.system;

import java.io.File;
import java.io.FileOutputStream;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.math.data_quality.DQResult;
import org.openlca.core.math.data_quality.DQSetup;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.ResultItemOrder;
import org.openlca.io.xls.results.CellWriter;
import org.openlca.io.xls.results.InfoSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultExport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(ResultExport.class);
	static final String[] FLOW_HEADER = {"Flow UUID", "Flow", "Category", "Sub-category", "Unit"};
	static final String[] PROCESS_HEADER = {"Process UUID", "Process", "Location"};
	static final String[] IMPACT_HEADER = {"Impact category UUID", "Impact category", "Reference unit"};
	static final String[] IMPACT_NW_HEADER = {"Normalized", "Weighted", "Single score unit"};

	private final File file;
	final CalculationSetup setup;
	final LcaResult result;
	final EntityCache cache;

	DQResult dqResult;
	private ResultItemOrder _items;
	private final EnumSet<MatrixPage> matrixPages = EnumSet.noneOf(MatrixPage.class);

	private boolean success;
	SXSSFWorkbook workbook;
	CellWriter writer;

	private final AtomicBoolean cancelled = new AtomicBoolean(false);
	boolean skipZeros = false;

	public ResultExport(CalculationSetup setup,
			LcaResult result, File file, EntityCache cache) {
		this.setup = setup;
		this.result = result;
		this.file = file;
		this.cache = cache;
	}

	public ResultExport withDqResult(DQResult dqResult) {
		this.dqResult = dqResult;
		return this;
	}

	public ResultExport withOrder(ResultItemOrder order) {
		this._items = order;
		return this;
	}

	public ResultExport addPage(MatrixPage page) {
		if (page != null) {
			matrixPages.add(page);
		}
		return this;
	}

	ResultItemOrder items() {
		if (_items == null) {
			_items = ResultItemOrder.of(result);
		}
		return _items;
	}

	public ResultExport skipZeros(boolean b) {
		skipZeros = b;
		return this;
	}

	public boolean wasCancelled() {
		return cancelled.get();
	}

	public void cancel() {
		cancelled.set(true);
	}

	@Override
	public void run() {
		try {
			workbook = new SXSSFWorkbook(-1);
			writer = new CellWriter(cache, workbook);
			DQSetup dqSetup = dqResult != null
					? dqResult.setup
					: null;
			InfoSheet.write(workbook, writer, setup, dqSetup, "Result information");
			if (result.hasEnviFlows() && !wasCancelled()) {
				InventorySheet.write(this);
			}
			if (result.hasImpacts() && !wasCancelled()) {
				ImpactSheet.write(this);
			}
			writeMatrices();
			success = true;
			if (!wasCancelled()) {
				try (var stream = new FileOutputStream(file)) {
					workbook.write(stream);
				}
			}
			workbook.dispose();
		} catch (Exception e) {
			log.error("Error exporting results", e);
			success = false;
		}
	}

	private void writeMatrices() {
		if (!result.hasEnviFlows())
			return;
		if (matrixPages.contains(MatrixPage.DIRECT_INVENTORIES)) {
			DirectInventoryMatrix.write(this, result);
		}
		if (matrixPages.contains(MatrixPage.TOTAL_INVENTORIES)) {
			TotalInventoryMatrix.write(this, result);
		}

		// impact matrices
		if (!result.hasImpacts())
			return;
		if (matrixPages.contains(MatrixPage.DIRECT_IMPACTS)) {
			DirectImpactMatrix.write(this, result);
		}
		if (matrixPages.contains(MatrixPage.FLOW_IMPACTS)) {
			FlowImpactMatrix.write(this, result);
		}
		if (matrixPages.contains(MatrixPage.TOTAL_IMPACTS)) {
			TotalImpactMatrix.write(this, result);
		}
	}

	public boolean doneWithSuccess() {
		return success;
	}

}
