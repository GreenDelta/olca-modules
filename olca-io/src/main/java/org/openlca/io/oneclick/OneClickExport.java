package org.openlca.io.oneclick;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.io.maps.FlowMap;
import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.io.xls.Excel;
import org.openlca.util.Dirs;
import org.openlca.util.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OneClickExport implements Runnable {

	private final IDatabase db;
	private final List<ProcessDescriptor> descriptors;
	private final File dir;

	private final TLongObjectHashMap<ProcessDescriptor> processes;
	private final UnitMatcher units;
	private PackagingMatcher packagingMatcher;
	private Map<String, FlowMapEntry> flowMap = Map.of();

	private OneClickExport(
		IDatabase db, List<ProcessDescriptor> descriptors, File dir
	) {
		this.db = db;
		this.descriptors = descriptors;
		this.dir = dir;
		this.processes = new ProcessDao(db).descriptorMap();
		this.units = UnitMatcher.create(db);
	}

	public static OneClickExport of(
		IDatabase db, List<ProcessDescriptor> descriptors, File dir
	) {
		return new OneClickExport(db, descriptors, dir);
	}

	public OneClickExport withPackagingMatcher(PackagingMatcher matcher) {
		this.packagingMatcher = matcher;
		return this;
	}

	public OneClickExport withFlowMap(FlowMap flowMap) {
		if (flowMap != null) {
			this.flowMap = flowMap.index();
		}
		return this;
	}

	@Override
	public void run() {
		Dirs.createIfAbsent(dir);
		if (packagingMatcher == null) {
			packagingMatcher = PackagingMatcher.createDefault();
		}

		for (var d : descriptors) {
			var process = db.get(Process.class, d.id);
			try (var workbook = new XSSFWorkbook()) {
				makeDataSheet(process, workbook);
				makeInfoSheet(process, workbook);
				writeFile(process, workbook);
			} catch (IOException e) {
				throw new RuntimeException("failed to create workbook", e);
			}
		}
	}

	private void makeDataSheet(Process process, Workbook workbook) {
		var sheet = workbook.createSheet("DATA");
		DataColumn.writeHeadersTo(sheet);
		int idx = 2;
		for (var e : process.exchanges) {
			if (skipDataRowOf(e, process))
				continue;
			var row = sheet.createRow(idx);
			var provider = e.defaultProviderId > 0
				? processes.get(e.defaultProviderId)
				: null;

			mapModule(e, provider, row);
			mapEpdNumber(e, provider, row);
			DataColumn.INPUT_QUANTITY.write(row, e.amount);
			DataColumn.QUANTITY.write(row, e.amount);
			DataColumn.UNIT.write(row, e.unit.name);
			DataColumn.FACTORY_LEVEL_DATA.write(row, "No");
			idx++;
		}
	}

	private void mapEpdNumber(Exchange e, ProcessDescriptor provider, Row row) {
		String tag, mappedId, altId;
		if (provider != null) {
			tag = provider.name + " (PROCESS)";
			mappedId = mappedIdOf(provider.refId);
			altId = provider.refId;
		} else {
			tag = e.flow.name + " (FLOW)";
			mappedId = mappedIdOf(e.flow.refId);
			altId = e.flow.refId;
		}
		DataColumn.RESOURCE.write(row, tag);
		if (mappedId != null) {
			DataColumn.EPD_NUMBER.write(row, mappedId);
			DataColumn.COMMENT.write(row, tag);
		} else {
			DataColumn.EPD_NUMBER.write(row, altId);
			DataColumn.COMMENT.write(row,
				tag + " openLCA UUID used as EPD number");
		}
	}

	private String mappedIdOf(String id) {
		var e = flowMap.get(id);
		if (e == null)
			return null;
		var mappedId = e.targetFlowId();
		return Strings.notEmpty(mappedId)
			? mappedId
			: null;
	}

	private void mapModule(Exchange e, ProcessDescriptor provider, Row row) {
		if (e.flow.flowType == FlowType.WASTE_FLOW) {
			Module.WASTE.writeTo(row);
		} else if (units.isEnergyUnit(e.unit)) {
			Module.ENERGY.writeTo(row);
		} else if (packagingMatcher.matches(provider)) {
			Module.PACKAGING.writeTo(row);
		} else {
			Module.MATERIALS.writeTo(row);
		}
	}

	private boolean skipDataRowOf(Exchange e, Process p) {
		return e == null
			|| e.equals(p.quantitativeReference)
			|| units.isTransportUnit(e.unit)
			|| e.flow == null
			|| e.unit == null;
	}

	private void makeInfoSheet(Process process, Workbook workbook) {
		var sheet = workbook.createSheet("INFO");
		Excel.cell(sheet, 0, 0, "Declared Unit");
		Excel.cell(sheet, 0, 1, "PRODUCT_UNIT_DECLARED");
		Excel.cell(sheet, 0, 2, Util.refQuantityOf(process));
		Excel.cell(sheet, 1, 0, "Mass per declared unit, kg");
		Excel.cell(sheet, 1, 1, "PRODUCT_UNIT_DECLARED_MASS");
		Excel.cell(sheet, 1, 2, Util.refMassOf(process));
	}

	private void writeFile(Process process, Workbook workbook) {
		var name = ("OneClickLCA " + process.name
			+ " (" + process.refId + ").xlsx")
			.replaceAll("[/\\\\:*?\"<>|]", "_");
		var file = new File(dir, name);
		try (var out = new FileOutputStream(file)) {
			workbook.write(out);
		} catch (IOException e) {
			throw new RuntimeException("failed to write file: " + name, e);
		}
	}

}
