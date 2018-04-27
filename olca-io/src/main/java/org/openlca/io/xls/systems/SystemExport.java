package org.openlca.io.xls.systems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.math.DataStructures;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.io.xls.Excel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemExport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Inventory inventory;
	private ImpactTable impactTable;
	private SystemExportConfig conf;

	public SystemExport(SystemExportConfig config) {
		this.conf = config;
	}

	public void exportTo(File dir) throws IOException {
		loadData();
		File subDir = new File(dir, conf.getSystem().getName().trim());
		if (!subDir.exists())
			subDir.mkdirs();
		createElementaryWorkbook(subDir);
		createProductWorkbook(subDir);
		if (impactTable != null)
			createImpactWorkbook(subDir);
	}

	private void loadData() {
		log.trace("load matrix data");
		inventory = DataStructures.createInventory(conf.getSystem(),
				conf.getAllocationMethod(), conf.getMatrixCache());
		if (conf.getImpactMethod() != null) {
			impactTable = ImpactTable.build(conf.getMatrixCache(), conf
					.getImpactMethod().getId(), inventory.flowIndex);
		}
	}

	private void createElementaryWorkbook(File subDir) throws IOException {
		log.trace("create workbook with elementary flows");
		Workbook elementaryWorkbook = new XSSFWorkbook();
		createElementaryCoverSheet(elementaryWorkbook,
				conf.getAllocationMethod());
		createElementarySheet(elementaryWorkbook);
		writeToFile(elementaryWorkbook, new File(subDir, FILE_NAMES.ELEMENTARY));
	}

	private void createProductWorkbook(File subDir) throws IOException {
		log.trace("create workbook with product flows");
		Workbook productWorkbook = new XSSFWorkbook();
		createProductCoverSheet(productWorkbook, conf.getAllocationMethod());
		createProductSheet(productWorkbook);
		writeToFile(productWorkbook, new File(subDir, FILE_NAMES.PRODUCT));
	}

	private void createImpactWorkbook(File subDir) throws IOException {
		log.trace("create workbook with impact assessment factors");
		Workbook impactMethodWorkbook = new XSSFWorkbook();
		createImpactMethodCoverSheet(impactMethodWorkbook);
		createImpactMethodSheet(impactMethodWorkbook);
		writeToFile(impactMethodWorkbook, new File(subDir,
				FILE_NAMES.IMPACT_FACTORS));
	}

	private void createElementaryCoverSheet(Workbook workbook,
			AllocationMethod allocationMethod) {
		Sheet sheet = workbook.createSheet("General information");
		boolean allocated = allocationMethod != null;
		String subTitle = allocated ? TITLES.ELEMENTARY_ALLOCATED
				: TITLES.ELEMENTARY;
		int currentRow = 0;
		currentRow = writeHeaderInformation(sheet, currentRow++, subTitle);
		currentRow = writeSoftwareInformation(sheet, currentRow++);

		String name = conf.getSystem().getName();
		int processes = conf.getSystem().processes.size();
		int products = inventory.productIndex.size();
		int flows = inventory.flowIndex.size();
		String dimensions = flows + "x" + products;

		currentRow = line(sheet, currentRow, "Product system:", name);
		if (allocated)
			currentRow = line(sheet, currentRow, "Allocation method:",
					getMethodLabel(allocationMethod));
		currentRow = line(sheet, currentRow, "No. of processes:", processes);
		currentRow = line(sheet, currentRow, "No. of products:", products);
		currentRow = line(sheet, currentRow, "No. of elementary flows:", flows);
		currentRow = line(sheet, currentRow, "Matrix dimensions:", dimensions);
		Excel.autoSize(sheet, 0, 1);
	}

	private String getMethodLabel(AllocationMethod method) {
		if (method == null)
			return "None";
		switch (method) {
		case CAUSAL:
			return "Causal";
		case ECONOMIC:
			return "Economic";
		case NONE:
			return "None";
		case PHYSICAL:
			return "Physical";
		case USE_DEFAULT:
			return "As defined in processes";
		default:
			return "Unknown";
		}
	}

	private void createProductCoverSheet(Workbook workbook,
			AllocationMethod allocationMethod) {
		Sheet sheet = workbook.createSheet("General information");

		boolean allocated = allocationMethod != null;
		String subTitle = allocated ? TITLES.PRODUCT_ALLOCATED : TITLES.PRODUCT;

		int currentRow = 0;
		currentRow = writeHeaderInformation(sheet, currentRow++, subTitle);
		currentRow = writeSoftwareInformation(sheet, currentRow++);

		String name = conf.getSystem().getName();
		int processes = conf.getSystem().processes.size();
		int products = inventory.productIndex.size();
		String dimensions = products + "x" + products;

		currentRow = line(sheet, currentRow, "Product system:", name);
		if (allocated)
			currentRow = line(sheet, currentRow, "Allocation method:",
					getMethodLabel(allocationMethod));
		currentRow = line(sheet, currentRow, "No. of processes:", processes);
		currentRow = line(sheet, currentRow, "No. of products:", products);
		currentRow = line(sheet, currentRow, "Matrix dimensions:", dimensions);

		Excel.autoSize(sheet, new int[] { 0, 1 });
	}

	private void createImpactMethodCoverSheet(Workbook workbook) {
		Sheet sheet = workbook.createSheet("General information");

		int row = 0;
		row = writeHeaderInformation(sheet, row, TITLES.IMPACT_FACTORS);
		row++;
		row = writeSoftwareInformation(sheet, row);
		row++;

		String name = conf.getSystem().getName();
		String method = conf.getImpactMethod().getName();
		int categories = impactTable.categoryIndex.size();
		int factors = impactTable.flowIndex.size();
		String dimensions = factors + "x" + categories;

		row = line(sheet, row, "Product system:", name);
		row = line(sheet, row, "Impact method:", method);
		row = line(sheet, row, "No. of impact categories:", categories);
		row = line(sheet, row, "No. of impact factors:", factors);
		row = line(sheet, row, "Matrix dimensions:", dimensions);

		Excel.autoSize(sheet, 0, 1);
	}

	private int writeHeaderInformation(Sheet sheet, int row, String title) {
		String date = DateFormat.getDateInstance().format(
				GregorianCalendar.getInstance().getTime());
		Excel.cell(sheet, row, 0, TITLES.MAIN_TITLE);
		row++;
		Excel.cell(sheet, row, 0, title);
		row++;
		row++;
		Excel.cell(sheet, row, 0, date);
		row++;
		return row;
	}

	private int writeSoftwareInformation(Sheet sheet, int row) {
		row = line(sheet, row, "Software:", "openLCA");
		row = line(sheet, row, "Version:", conf.getOlcaVersion());
		row = line(sheet, row, "Database:", conf.getDatabase().getName());
		return row;
	}

	private int line(Sheet sheet, int row, String label, String value) {
		Excel.cell(sheet, row, 0, label);
		Excel.cell(sheet, row, 1, value);
		return row + 1;
	}

	private int line(Sheet sheet, int row, String label, double value) {
		Excel.cell(sheet, row, 0, label);
		Excel.cell(sheet, row, 1, value);
		return row + 1;
	}

	private ExcelHeader createFlowHeader(FlowIndex index) {
		ExcelHeader header = new ExcelHeader();
		header.setHeaders(HEADERS.FLOW.VALUES);
		List<IExcelHeaderEntry> entries = new ArrayList<>();
		List<FlowInfo> sortedFlows = mapFlowIndices(header, index);
		for (FlowInfo info : sortedFlows) {
			entries.add(new FlowHeaderEntry(info));
		}
		header.setEntries(entries.toArray(new IExcelHeaderEntry[entries.size()]));
		return header;
	}

	private ExcelHeader createProductHeader(TechIndex index) {
		ExcelHeader header = new ExcelHeader();
		header.setHeaders(HEADERS.PRODUCT.VALUES);
		List<IExcelHeaderEntry> headerEntries = new ArrayList<>();
		List<ProductInfo> sortedProducts = mapProductIndices(header,
				index);
		for (ProductInfo product : sortedProducts) {
			headerEntries.add(new ProductHeaderEntry(product));
		}
		header.setEntries(headerEntries
				.toArray(new IExcelHeaderEntry[headerEntries.size()]));
		return header;
	}

	private ExcelHeader createImpactCategoryHeader(LongIndex impactIndex) {
		ExcelHeader header = new ExcelHeader();
		header.setHeaders(HEADERS.IMPACT_CATEGORY.VALUES);
		List<IExcelHeaderEntry> headerEntries = new ArrayList<>();
		List<ImpactCategoryDescriptor> sortedCategories = mapImpactCategoryIndices(
				header, impactIndex);
		for (ImpactCategoryDescriptor category : sortedCategories) {
			headerEntries.add(new ImpactCategoryHeaderEntry(conf
					.getImpactMethod().getName(), category));
		}
		header.setEntries(headerEntries
				.toArray(new IExcelHeaderEntry[headerEntries.size()]));
		return header;
	}

	private void createElementarySheet(Workbook workbook) {
		ExcelHeader columnHeader = createProductHeader(inventory.productIndex);
		ExcelHeader rowHeader = createFlowHeader(inventory.flowIndex);
		MatrixExcelExport export = new MatrixExcelExport();
		export.setColumnHeader(columnHeader);
		export.setRowHeader(rowHeader);
		export.setMatrix(inventory.interventionMatrix.createRealMatrix(
				conf.getSolver()));
		export.writeTo(workbook);
	}

	private void createProductSheet(Workbook workbook) {
		ExcelHeader columnHeader = createProductHeader(inventory.productIndex);
		ExcelHeader rowHeader = createProductHeader(inventory.productIndex);
		MatrixExcelExport export = new MatrixExcelExport();
		export.setColumnHeader(columnHeader);
		export.setRowHeader(rowHeader);
		export.setMatrix(inventory.technologyMatrix.createRealMatrix(
				conf.getSolver()));
		Sheet sheet = export.writeTo(workbook);
		int columnOffSet = rowHeader.getHeaderSize() + 1;
		for (int i = 0; i < columnHeader.getHeaderSize(); i++) {
			Excel.headerStyle(workbook, sheet, i, columnOffSet);
		}
	}

	private void createImpactMethodSheet(Workbook workbook) {
		ExcelHeader columnHeader = createImpactCategoryHeader(
				impactTable.categoryIndex);
		ExcelHeader rowHeader = createFlowHeader(impactTable.flowIndex);
		MatrixExcelExport export = new MatrixExcelExport();
		export.setColumnHeader(columnHeader);
		export.setRowHeader(rowHeader);
		export.setMatrix(transpose(impactTable.factorMatrix
				.createRealMatrix(conf.getSolver())));
		export.writeTo(workbook);
	}

	private List<FlowInfo> mapFlowIndices(ExcelHeader header,
			FlowIndex flowIndex) {
		List<FlowInfo> sortedFlows = FlowInfo.getAll(conf, flowIndex);
		Collections.sort(sortedFlows);
		int counter = 0;
		for (FlowInfo flow : sortedFlows) {
			header.putIndexMapping(counter,
					flowIndex.getIndex(flow.getRealId()));
			counter++;
		}
		return sortedFlows;
	}

	private List<ProductInfo> mapProductIndices(ExcelHeader header, TechIndex index) {
		List<ProductInfo> products = ProductInfo.getAll(conf, index);
		Collections.sort(products);
		int i = 0;
		for (ProductInfo product : products) {
			header.putIndexMapping(i, index.getIndex(product.getLongPair()));
			i++;
		}
		return products;
	}

	private List<ImpactCategoryDescriptor> mapImpactCategoryIndices(
			ExcelHeader header, LongIndex impactIndex) {
		Set<ImpactCategoryDescriptor> impacts = getImpacts(impactIndex,
				conf.getEntityCache());
		List<ImpactCategoryDescriptor> sortedCategories = new ArrayList<>(
				impacts);
		Collections.sort(sortedCategories);
		int counter = 0;
		for (ImpactCategoryDescriptor category : sortedCategories) {
			header.putIndexMapping(counter,
					impactIndex.getIndex(category.getId()));
			counter++;
		}
		return sortedCategories;
	}

	private Set<ImpactCategoryDescriptor> getImpacts(LongIndex index,
			EntityCache cache) {
		if (index == null)
			return Collections.emptySet();
		List<Long> ids = new ArrayList<>(index.size());
		for (long id : index.getKeys())
			ids.add(id);
		Map<Long, ImpactCategoryDescriptor> values = cache.getAll(
				ImpactCategoryDescriptor.class, ids);
		HashSet<ImpactCategoryDescriptor> descriptors = new HashSet<>();
		descriptors.addAll(values.values());
		return descriptors;
	}

	private IMatrix transpose(IMatrix matrix) {
		IMatrix result = conf.getSolver().matrix(
				matrix.columns(), matrix.rows());
		for (int row = 0; row < matrix.rows(); row++) {
			for (int column = 0; column < matrix.columns(); column++) {
				double value = matrix.get(row, column);
				result.set(column, row, value);
			}
		}
		return result;
	}

	private void writeToFile(Workbook workbook, File file) throws IOException {
		int i = 1;
		File actFile = new File(file.getAbsolutePath());
		while (actFile.exists()) {
			String tmp = file.getAbsolutePath();
			tmp = tmp.substring(0, tmp.lastIndexOf('.')) + "(" + i + ")"
					+ tmp.substring(tmp.lastIndexOf('.'));
			actFile = new File(tmp);
			i++;
		}
		actFile.createNewFile();
		log.trace("write file {}", actFile.getAbsolutePath());
		try (FileOutputStream fos = new FileOutputStream(actFile)) {
			workbook.write(fos);
		}
	}

	private interface TITLES {

		String MAIN_TITLE = "OpenLCA Life Cycle Assessment Matrix Export";

		String ELEMENTARY = "Elementary Flows Associated with Processes/Activities, no allocation applied";
		String ELEMENTARY_ALLOCATED = "Elementary Flows Associated with Processes/Activities, after allocation";
		String PRODUCT = "Use of products/services by processes/activities without allocation or co-product/avoided production credits";
		String PRODUCT_ALLOCATED = "Use of Products/Services by Processes/Activities with user-specified allocation or co-product/avoided production applied";
		String IMPACT_FACTORS = "Life Cycle Impact Assessment, Characterization Factors";

	}

	private interface FILE_NAMES {

		String ELEMENTARY = "ElementaryFlows.xlsx";
		String PRODUCT = "ProductFlows.xlsx";
		String IMPACT_FACTORS = "ImpactFactors.xlsx";

	}

	private interface HEADERS {

		interface FLOW {

			String CATEGORY = "Category";
			String LOCATION = "Elementary flow location";
			String NAME = "Elementary flowname";
			String SUB_CATEGORY = "Sub category";
			String UNIT = "Unit";
			String UUID = "UUID";

			String[] VALUES = new String[] { UUID, CATEGORY, SUB_CATEGORY,
					NAME, LOCATION, UNIT };

		}

		interface PRODUCT {

			String INFRASTRUCTURE_PRODUCT = "Infrastructure product";
			String MULTI_OUTPUT = "Multi-Output process";
			String PROCESS_CATEGORY = "Process category";
			String PROCESS_LOCATION = "Process location";
			String PROCESS_NAME = "Process name";
			String PROCESS_SUB_CATEGORY = "Process sub category";
			String PRODUCT_NAME = "Product name";
			String PRODUCT_UNIT = "Product/Service unit";
			String UUID = "UUID";

			String[] VALUES = new String[] { PROCESS_NAME, PRODUCT_NAME,
					MULTI_OUTPUT, UUID, INFRASTRUCTURE_PRODUCT,
					PROCESS_LOCATION, PROCESS_CATEGORY, PROCESS_SUB_CATEGORY,
					PRODUCT_UNIT };

		}

		interface IMPACT_CATEGORY {

			String CATEGORY = "Sub category";
			String METHOD = "Category";
			String UNIT = "Unit";
			String UUID = "UUID";

			String[] VALUES = new String[] { UUID, CATEGORY, METHOD, UNIT };

		}

	}

	private class FlowHeaderEntry implements IExcelHeaderEntry {

		private FlowInfo flowInfo;

		private FlowHeaderEntry(FlowInfo flowInfo) {
			this.flowInfo = flowInfo;
		}

		@Override
		public String getValue(int count) {
			if (count > HEADERS.FLOW.VALUES.length)
				return null;
			String header = HEADERS.FLOW.VALUES[count];
			return getValue(header);
		}

		private String getValue(String header) {
			switch (header) {
			case HEADERS.FLOW.NAME:
				return flowInfo.getName();
			case HEADERS.FLOW.UUID:
				return flowInfo.getId();
			case HEADERS.FLOW.LOCATION:
				return flowInfo.getLocation();
			case HEADERS.FLOW.CATEGORY:
				return flowInfo.getCategory();
			case HEADERS.FLOW.SUB_CATEGORY:
				return flowInfo.getSubCategory();
			case HEADERS.FLOW.UNIT:
				return flowInfo.getUnit();
			}
			return null;
		}

	}

	private class ProductHeaderEntry implements IExcelHeaderEntry {

		private ProductInfo productInfo;

		private ProductHeaderEntry(ProductInfo productInfo) {
			this.productInfo = productInfo;
		}

		@Override
		public String getValue(int count) {
			if (count > HEADERS.PRODUCT.VALUES.length)
				return null;
			String header = HEADERS.PRODUCT.VALUES[count];
			return getValue(header);
		}

		private String getValue(String header) {
			switch (header) {
			case HEADERS.PRODUCT.PROCESS_NAME:
				return productInfo.getProcess();
			case HEADERS.PRODUCT.PRODUCT_NAME:
				return productInfo.getProduct();
			case HEADERS.PRODUCT.MULTI_OUTPUT:
				return Boolean.toString(productInfo.isFromMultiOutputProcess());
			case HEADERS.PRODUCT.UUID:
				return productInfo.getProductId();
			case HEADERS.PRODUCT.INFRASTRUCTURE_PRODUCT:
				return Boolean.toString(productInfo
						.isFromInfrastructureProcess());
			case HEADERS.PRODUCT.PROCESS_LOCATION:
				return productInfo.getProcessLocation();
			case HEADERS.PRODUCT.PROCESS_CATEGORY:
				return productInfo.getProcessCategory();
			case HEADERS.PRODUCT.PROCESS_SUB_CATEGORY:
				return productInfo.getProcessSubCategory();
			case HEADERS.PRODUCT.PRODUCT_UNIT:
				return productInfo.getProductUnit();
			}
			return null;
		}

	}

	private class ImpactCategoryHeaderEntry implements IExcelHeaderEntry {

		private ImpactCategoryDescriptor impactCategory;
		private String methodName;

		private ImpactCategoryHeaderEntry(String methodName,
				ImpactCategoryDescriptor impactCategory) {
			this.methodName = methodName;
			this.impactCategory = impactCategory;
		}

		@Override
		public String getValue(int count) {
			if (count > HEADERS.IMPACT_CATEGORY.VALUES.length)
				return null;
			String header = HEADERS.IMPACT_CATEGORY.VALUES[count];
			return getValue(header);
		}

		private String getValue(String header) {
			switch (header) {
			case HEADERS.IMPACT_CATEGORY.CATEGORY:
				return impactCategory.getName();
			case HEADERS.IMPACT_CATEGORY.UUID:
				return impactCategory.getRefId();
			case HEADERS.IMPACT_CATEGORY.METHOD:
				return methodName;
			case HEADERS.IMPACT_CATEGORY.UNIT:
				return impactCategory.getReferenceUnit();
			}
			return null;
		}

	}

}
