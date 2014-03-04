package org.openlca.io.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.InterpreterException;
import org.openlca.io.KeyGen;
import org.openlca.io.maps.ImportMap;
import org.openlca.io.maps.MapType;
import org.openlca.io.maps.MappingBuilder;
import org.openlca.io.maps.content.CSVElementaryCategoryContent;
import org.openlca.io.maps.content.SPElementaryFlowContent;
import org.openlca.io.maps.content.CSVProductFlowContent;
import org.openlca.simapro.csv.model.SPCalculatedParameter;
import org.openlca.simapro.csv.model.SPDataSet;
import org.openlca.simapro.csv.model.SPInputParameter;
import org.openlca.simapro.csv.model.SPLiteratureReference;
import org.openlca.simapro.csv.model.SPLiteratureReferenceEntry;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPProcessDocumentation;
import org.openlca.simapro.csv.model.SPWasteScenario;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private CSVImportCache cache;
	private Process process;
	private SPDataSet dataEntry;
	private ProcessDao processDao;
	private LocationDao locationDao;
	private IDatabase database;
	private SourceDao sourceDao;
	private FormulaInterpreter interpreter;
	private long scopeId = 1000;
	private boolean useRefNameForProcess;
	private ImportMap<SPElementaryFlowContent> elemFlowMap;
	private ImportMap<CSVProductFlowContent> productFlowMap;
	private ImportMap<CSVElementaryCategoryContent> categoryMap;

	ProcessImport(IDatabase database, FormulaInterpreter interpreter,
			boolean useRefNameForProcess) {
		init(database, interpreter);
	}

	ProcessImport(IDatabase database, FormulaInterpreter interpreter,
			CSVImportCache cache, boolean useRefNameForProcess) {
		this.cache = cache;
		init(database, interpreter);
	}

	void setCache(CSVImportCache cache) {
		this.cache = cache;
	}

	void init(IDatabase database, FormulaInterpreter interpreter) {
		this.database = database;
		this.interpreter = interpreter;
		processDao = new ProcessDao(database);
		locationDao = new LocationDao(database);
		sourceDao = new SourceDao(database);
		MappingBuilder mappingBuilder = new MappingBuilder(database);
		elemFlowMap = mappingBuilder.buildImportMapping(
				SPElementaryFlowContent.class, MapType.CSV_ELEMENTARY_FLOW);
		productFlowMap = mappingBuilder.buildImportMapping(
				CSVProductFlowContent.class, MapType.CSV_PRODUCT_FLOW);
		categoryMap = mappingBuilder.buildImportMapping(
				CSVElementaryCategoryContent.class, MapType.CSV_CATEGORY);
	}

	void runImport(SPProcess process) throws Exception {
		dataEntry = process;
		convert();
	}

	void runImport(SPWasteTreatment wasteTreatment) throws Exception {
		dataEntry = wasteTreatment;
		convert();
	}

	private void convert() throws InterpreterException {
		process = new Process();
		if (!validate())
			return;
		scopeId++;
		setDefaults();
		processType();
		location();
		sources();
		parameters();
		new FlowImport(database, cache, interpreter, scopeId, elemFlowMap,
				productFlowMap, categoryMap).importFlows(process, dataEntry);
		processDao.insert(process);
	}

	private boolean validate() {
		// TODO: maybe better handling
		if (checkAndSetRefId()) {
			log.info("Process will not be imported because it already exists: "
					+ dataEntry.getDocumentation().getName());
			return false;
		}
		if (dataEntry == null) {
			log.warn("dataEntry object is null.");
			return false;
		}
		if (dataEntry.getDocumentation() == null) {
			log.warn("Documentation in null.");
			return false;
		}
		return true;
	}

	private boolean checkAndSetRefId() {
		setProcessName();
		String refId = CSVKeyGen.forProcess(process.getName());
		if (processDao.getForRefId(refId) != null)
			return true;
		process.setRefId(refId);
		return false;
	}

	private void setDefaults() {
		ProcessDocumentation doc = new ProcessDocumentation();
		process.setDocumentation(doc);
		SPProcessDocumentation spDoc = dataEntry.getDocumentation();
		doc.setTime(spDoc.getTimePeriod().getValue());
		doc.setTechnology(spDoc.getTechnology().getValue());
		process.setInfrastructureProcess(spDoc.isInfrastructureProcess());
		if (Utils.nullCheck(spDoc.getDataTreatment()))
			doc.setDataTreatment(spDoc.getDataTreatment());
		setDescription(spDoc);
		process.setCategory(Utils.createCategoryTree(database, categoryMap,
				ModelType.PROCESS, dataEntry.getDocumentation().getCategory()
						.getValue(), dataEntry.getSubCategory()));
	}

	private void setProcessName() {
		if (dataEntry.getDocumentation().getName() == null
				|| "".equals(dataEntry.getDocumentation().getName())
				|| useRefNameForProcess) {
			if (dataEntry instanceof SPProcess) {
				SPProcess process = (SPProcess) dataEntry;
				this.process.setName(process.getReferenceProduct().getName());
			} else if (dataEntry instanceof SPWasteTreatment) {
				SPWasteTreatment treatment = (SPWasteTreatment) dataEntry;
				process.setName(treatment.getWasteSpecification().getName());
			} else if (database instanceof SPWasteScenario) {
				// TODO:
			}
		} else {
			process.setName(dataEntry.getDocumentation().getName());
		}
	}

	private void setDescription(SPProcessDocumentation spDocumentation) {
		if (spDocumentation == null)
			return;
		StringBuilder builder = new StringBuilder();
		if (Utils.nullCheck(spDocumentation.getComment()))
			appendWithNewLine(spDocumentation.getComment(), builder);
		if (spDocumentation.getStatus() != null)
			appendWithNewLine("Status: "
					+ spDocumentation.getStatus().getValue(), builder);
		if (spDocumentation.getRepresentativeness() != null)
			appendWithNewLine("Representativeness: "
					+ spDocumentation.getRepresentativeness().getValue(),
					builder);
		if (spDocumentation.getCutOffRule() != null)
			appendWithNewLine("Cut off rules: "
					+ spDocumentation.getCutOffRule().getValue(), builder);
		if (spDocumentation.getProcessAllocation() != null)
			appendWithNewLine("Multiple output allocation: "
					+ spDocumentation.getProcessAllocation().getValue(),
					builder);
		if (spDocumentation.getWasteTreatmentAllocation() != null)
			appendWithNewLine(
					"Waste treatment allocation: "
							+ spDocumentation.getWasteTreatmentAllocation(),
					builder);
		if (spDocumentation.getSubstitution() != null)
			appendWithNewLine("Substitution allocation: "
					+ spDocumentation.getSubstitution().getValue(), builder);
		if (spDocumentation.getSystemBoundary() != null)
			appendWithNewLine("Capital goods: "
					+ spDocumentation.getSystemBoundary().getValue(), builder);
		if (spDocumentation.getBoundaryWithNature() != null)
			appendWithNewLine("Boundary with nature: "
					+ spDocumentation.getBoundaryWithNature().getValue(),
					builder);
		if (Utils.nullCheck(spDocumentation.getRecord()))
			appendWithNewLine("Record: " + spDocumentation.getRecord(), builder);
		if (Utils.nullCheck(spDocumentation.getGenerator()))
			appendWithNewLine("Generator: " + spDocumentation.getGenerator(),
					builder);
		if (Utils.nullCheck(spDocumentation.getCollectionMethod()))
			appendWithNewLine(
					"Collection Method: "
							+ spDocumentation.getCollectionMethod(), builder);
		if (Utils.nullCheck(spDocumentation.getVerification()))
			appendWithNewLine(
					"Verification: " + spDocumentation.getVerification(),
					builder);
		if (Utils.nullCheck(spDocumentation.getAllocationRules()))
			appendWithNewLine(
					"Allocation rules: " + spDocumentation.getAllocationRules(),
					builder);
		process.setDescription(builder.toString());
	}

	private void appendWithNewLine(String line, StringBuilder builder) {
		if (builder == null || line == null)
			return;
		if ("".equals(builder.toString()))
			builder.append(line);
		else
			builder.append("\n\n" + line);
	}

	private void sources() {
		for (SPLiteratureReferenceEntry entry : dataEntry.getDocumentation()
				.getLiteratureReferenceEntries())
			process.getDocumentation().getSources()
					.add(findOrCreate(entry.getLiteratureReference()));
	}

	private void processType() {
		org.openlca.simapro.csv.model.enums.ProcessType processType = dataEntry
				.getDocumentation().getProcessType();
		if (processType != null
				&& processType == org.openlca.simapro.csv.model.enums.ProcessType.SYSTEM) {
			process.setProcessType(ProcessType.LCI_RESULT);
		} else {
			process.setProcessType(ProcessType.UNIT_PROCESS);
		}
	}

	private void location() {
		String geo = dataEntry.getDocumentation().getGeography().getValue();
		String refId = KeyGen.get(geo);
		Location location = locationDao.getForRefId(refId);
		if (location == null) {
			location = new Location();
			location.setRefId(refId);
			location.setName(geo);
			location.setDescription("SimaPro location");
			locationDao.insert(location);
		}
		process.setLocation(location);

	}

	private void parameters() throws InterpreterException {
		interpreter.createScope(scopeId);
		for (SPCalculatedParameter parameter : dataEntry
				.getCalculatedParameters()) {
			process.getParameters().add(
					Utils.create(parameter, ParameterScope.PROCESS));
			interpreter.getScope(scopeId).bind(parameter.getName(),
					parameter.getExpression());
		}
		for (SPInputParameter parameter : dataEntry.getInputParameters()) {
			process.getParameters().add(
					Utils.create(parameter, ParameterScope.PROCESS));
			interpreter.getScope(scopeId).bind(parameter.getName(),
					String.valueOf(parameter.getValue()));
		}
		for (Parameter parameter : process.getParameters())
			if (!parameter.isInputParameter())
				parameter.setValue(interpreter.getScope(scopeId).eval(
						parameter.getFormula()));
	}

	private Source findOrCreate(SPLiteratureReference literatureReference) {
		String refId = CSVKeyGen.forSource(literatureReference);
		Source source = sourceDao.getForRefId(refId);
		if (source != null)
			return source;
		source = new Source();
		source.setRefId(refId);
		source.setName(literatureReference.getName());
		source.setCategory(Utils.createCategoryTree(database, categoryMap,
				ModelType.SOURCE, null, literatureReference.getCategory()));
		StringBuilder comment = new StringBuilder();
		if (Utils.nullCheck(literatureReference.getContent()))
			comment.append(literatureReference.getContent());
		if (Utils.nullCheck(literatureReference.getDocumentLink()))
			comment.append("\n" + literatureReference.getDocumentLink());
		if (!comment.toString().equals(""))
			source.setDescription(comment.toString());
		sourceDao.insert(source);
		return source;
	}
}
