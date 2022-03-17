package org.openlca.io.xls.systems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SystemExport {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private MatrixData data;
	private final SystemExportConfig conf;

	public SystemExport(SystemExportConfig config) {
		this.conf = config;
	}

	public void exportTo(File dir) throws IOException {
		var setup = CalculationSetup.simple(conf.system)
			// TODO: .withParameters(conf.system.parameterRedefs)
			.withAllocation(conf.allocationMethod);
		// setup.impactMethod = conf.impactMethod;
		var techIndex = TechIndex.of(conf.database,  setup);
		data = MatrixData.of(conf.database, techIndex)
				.withSetup(setup)
				.build();

		File subDir = new File(dir, conf.system.name.trim());
		if (!subDir.exists())
			subDir.mkdirs();
		createElementaryWorkbook(subDir);
		createProductWorkbook(subDir);
		if (data.impactMatrix != null) {
			createImpactWorkbook(subDir);
		}
	}

	private void createElementaryWorkbook(File subDir) throws IOException {
		log.trace("create workbook with elementary flows");
		Workbook elementaryWorkbook = new XSSFWorkbook();
		createElementaryCoverSheet(elementaryWorkbook,
			conf.allocationMethod);
		createElementarySheet(elementaryWorkbook);
		writeToFile(elementaryWorkbook,
			new File(subDir, FILE_NAMES.ELEMENTARY));
	}

	private void createProductWorkbook(File subDir) throws IOException {
		log.trace("create workbook with product flows");
		Workbook productWorkbook = new XSSFWorkbook();
		createProductCoverSheet(productWorkbook, conf.allocationMethod);
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
		Excel.trackSize(sheet, 0, 1);
		boolean allocated = allocationMethod != null;
		String subTitle = allocated
			? TITLES.ELEMENTARY_ALLOCATED
			: TITLES.ELEMENTARY;
		int row = 0;
		row = writeHeaderInformation(sheet, row++, subTitle);
		row = writeSoftwareInformation(sheet, row++);

		String name = conf.system.name;
		int processes = conf.system.processes.size();
		int products = data.techIndex.size();
		int flows = data.enviIndex.size();
		String dimensions = flows + "x" + products;

		row = line(sheet, row, "Product system:", name);
		if (allocated)
			row = line(sheet, row, "Allocation method:",
				getMethodLabel(allocationMethod));
		row = line(sheet, row, "No. of processes:", processes);
		row = line(sheet, row, "No. of products:", products);
		row = line(sheet, row, "No. of elementary flows:", flows);
		row = line(sheet, row, "Matrix dimensions:", dimensions);
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
		Excel.trackSize(sheet, 0, 1);
		boolean allocated = allocationMethod != null;
		String subTitle = allocated
			? TITLES.PRODUCT_ALLOCATED
			: TITLES.PRODUCT;

		int row = 0;
		row = writeHeaderInformation(sheet, row++, subTitle);
		row = writeSoftwareInformation(sheet, row++);

		String name = conf.system.name;
		int processes = conf.system.processes.size();
		int products = data.techIndex.size();
		String dimensions = products + "x" + products;

		row = line(sheet, row, "Product system:", name);
		if (allocated)
			row = line(sheet, row, "Allocation method:",
				getMethodLabel(allocationMethod));
		row = line(sheet, row, "No. of processes:", processes);
		row = line(sheet, row, "No. of products:", products);
		row = line(sheet, row, "Matrix dimensions:", dimensions);

		Excel.autoSize(sheet, 0, 1);
	}

	private void createImpactMethodCoverSheet(Workbook workbook) {
		Sheet sheet = workbook.createSheet("General information");
		Excel.trackSize(sheet, 0, 1);
		int row = 0;
		row = writeHeaderInformation(sheet, row, TITLES.IMPACT_FACTORS);
		row++;
		row = writeSoftwareInformation(sheet, row);
		row++;

		String name = conf.system.name;
		String method = conf.impactMethod.name;
		int categories = data.impactIndex.size();
		int factors = data.enviIndex.size();
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
		row = line(sheet, row, "Version:", conf.olcaVersion);
		row = line(sheet, row, "Database:", conf.database.getName());
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

	private ExcelHeader createFlowHeader(EnviIndex index) {
		ExcelHeader header = new ExcelHeader();
		header.setHeaders(HEADERS.FLOW.VALUES);
		List<IExcelHeaderEntry> entries = new ArrayList<>();
		List<FlowInfo> sortedFlows = mapFlowIndices(header, index);
		for (FlowInfo info : sortedFlows) {
			entries.add(new FlowHeaderEntry(info));
		}
		header.setEntries(entries.toArray(new IExcelHeaderEntry[0]));
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

	private ExcelHeader createImpactCategoryHeader(ImpactIndex impactIndex) {
		ExcelHeader header = new ExcelHeader();
		header.setHeaders(HEADERS.IMPACT_CATEGORY.VALUES);
		List<IExcelHeaderEntry> headerEntries = new ArrayList<>();
		List<ImpactDescriptor> sortedCategories = mapImpactCategoryIndices(
			header, impactIndex);
		for (ImpactDescriptor category : sortedCategories) {
			headerEntries.add(new ImpactCategoryHeaderEntry(
				conf.impactMethod.name, category));
		}
		header.setEntries(headerEntries
			.toArray(new IExcelHeaderEntry[headerEntries.size()]));
		return header;
	}

	private void createElementarySheet(Workbook workbook) {
		ExcelHeader columnHeader = createProductHeader(data.techIndex);
		ExcelHeader rowHeader = createFlowHeader(data.enviIndex);
		MatrixExcelExport export = new MatrixExcelExport();
		export.setColumnHeader(columnHeader);
		export.setRowHeader(rowHeader);
		export.setMatrix(data.enviMatrix);
		export.writeTo(workbook);
	}

	private void createProductSheet(Workbook workbook) {
		ExcelHeader columnHeader = createProductHeader(data.techIndex);
		ExcelHeader rowHeader = createProductHeader(data.techIndex);
		MatrixExcelExport export = new MatrixExcelExport();
		export.setColumnHeader(columnHeader);
		export.setRowHeader(rowHeader);
		export.setMatrix(data.techMatrix);
		Sheet sheet = export.writeTo(workbook);
		int columnOffSet = rowHeader.getHeaderSize() + 1;
		for (int i = 0; i < columnHeader.getHeaderSize(); i++) {
			Excel.headerStyle(workbook, sheet, i, columnOffSet);
		}
	}

	private void createImpactMethodSheet(Workbook workbook) {
		ExcelHeader columnHeader = createImpactCategoryHeader(
			data.impactIndex);
		ExcelHeader rowHeader = createFlowHeader(data.enviIndex);
		MatrixExcelExport export = new MatrixExcelExport();
		export.setColumnHeader(columnHeader);
		export.setRowHeader(rowHeader);
		export.setMatrix(transpose(data.impactMatrix));
		export.writeTo(workbook);
	}

	private List<FlowInfo> mapFlowIndices(ExcelHeader header,
		EnviIndex flowIndex) {
		List<FlowInfo> sortedFlows = FlowInfo.getAll(conf, flowIndex);
		Collections.sort(sortedFlows);
		int counter = 0;
		for (FlowInfo flow : sortedFlows) {
			header.putIndexMapping(counter, flowIndex.of(flow.realId));
			counter++;
		}
		return sortedFlows;
	}

	private List<ProductInfo> mapProductIndices(ExcelHeader header,
		TechIndex index) {
		List<ProductInfo> products = ProductInfo.getAll(conf, index);
		Collections.sort(products);
		int i = 0;
		for (ProductInfo product : products) {
			header.putIndexMapping(i, index.of(product.provider));
			i++;
		}
		return products;
	}

	private List<ImpactDescriptor> mapImpactCategoryIndices(
		ExcelHeader header, ImpactIndex impactIndex) {
		var sortedCategories = impactIndex.content()
			.stream()
			.sorted((i1, i2) -> Strings.compare(i1.name, i2.name))
			.collect(Collectors.toList());
		int counter = 0;
		for (ImpactDescriptor category : sortedCategories) {
			header.putIndexMapping(counter, impactIndex.of(category));
			counter++;
		}
		return sortedCategories;
	}

	private Matrix transpose(MatrixReader matrix) {
		var result = new DenseMatrix(matrix.columns(), matrix.rows());
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

			String[] VALUES = new String[]{UUID, CATEGORY, SUB_CATEGORY,
				NAME, LOCATION, UNIT};

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

			String[] VALUES = new String[]{PROCESS_NAME, PRODUCT_NAME,
				MULTI_OUTPUT, UUID, INFRASTRUCTURE_PRODUCT,
				PROCESS_LOCATION, PROCESS_CATEGORY, PROCESS_SUB_CATEGORY,
				PRODUCT_UNIT};

		}

		interface IMPACT_CATEGORY {

			String CATEGORY = "Sub category";
			String METHOD = "Category";
			String UNIT = "Unit";
			String UUID = "UUID";

			String[] VALUES = new String[]{UUID, CATEGORY, METHOD, UNIT};

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
					return flowInfo.name;
				case HEADERS.FLOW.UUID:
					return flowInfo.id;
				case HEADERS.FLOW.LOCATION:
					return flowInfo.location;
				case HEADERS.FLOW.CATEGORY:
					return flowInfo.category;
				case HEADERS.FLOW.SUB_CATEGORY:
					return flowInfo.subCategory;
				case HEADERS.FLOW.UNIT:
					return flowInfo.unit;
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
					return productInfo.process;
				case HEADERS.PRODUCT.PRODUCT_NAME:
					return productInfo.product;
				case HEADERS.PRODUCT.MULTI_OUTPUT:
					return Boolean.toString(productInfo.fromMultiOutputProcess);
				case HEADERS.PRODUCT.UUID:
					return productInfo.productId;
				case HEADERS.PRODUCT.INFRASTRUCTURE_PRODUCT:
					return Boolean.toString(productInfo.fromInfrastructureProcess);
				case HEADERS.PRODUCT.PROCESS_LOCATION:
					return productInfo.processLocation;
				case HEADERS.PRODUCT.PROCESS_CATEGORY:
					return productInfo.processCategory;
				case HEADERS.PRODUCT.PROCESS_SUB_CATEGORY:
					return productInfo.processSubCategory;
				case HEADERS.PRODUCT.PRODUCT_UNIT:
					return productInfo.productUnit;
			}
			return null;
		}

	}

	private class ImpactCategoryHeaderEntry implements IExcelHeaderEntry {

		private ImpactDescriptor impactCategory;
		private String methodName;

		private ImpactCategoryHeaderEntry(String methodName,
			ImpactDescriptor impactCategory) {
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
					return impactCategory.name;
				case HEADERS.IMPACT_CATEGORY.UUID:
					return impactCategory.refId;
				case HEADERS.IMPACT_CATEGORY.METHOD:
					return methodName;
				case HEADERS.IMPACT_CATEGORY.UNIT:
					return impactCategory.referenceUnit;
			}
			return null;
		}

	}

}
