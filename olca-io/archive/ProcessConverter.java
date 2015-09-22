package org.openlca.io.csv.output;

import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.PedigreeMatrix;
import org.openlca.core.model.PedigreeMatrixRow;
import org.openlca.core.model.Process;
import org.openlca.core.model.Source;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.UnitMapping;
import org.openlca.io.maps.MapType;
import org.openlca.io.maps.MappingBuilder;
import org.openlca.io.maps.content.CSVElementaryCategoryContent;
import org.openlca.io.maps.content.SPElementaryFlowContent;
import org.openlca.io.maps.content.CSVGeographyContent;
import org.openlca.io.maps.content.CSVQuantityContent;
import org.openlca.io.maps.content.CSVUnitContent;
import org.openlca.simapro.csv.model.IDistribution;
import org.openlca.simapro.csv.model.SPLogNormalDistribution;
import org.openlca.simapro.csv.model.SPNormalDistribution;
import org.openlca.simapro.csv.model.SPPedigreeMatrix;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPProcessDocumentation;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.SPTriangleDistribution;
import org.openlca.simapro.csv.model.SPUniformDistribution;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.Geography;
import org.openlca.simapro.csv.model.enums.ProcessCategory;
import org.openlca.simapro.csv.model.enums.ProcessType;
import org.openlca.simapro.csv.model.enums.ProductType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.process.LiteratureReferenceRow;
import org.openlca.simapro.csv.model.process.ProductExchangeRow;
import org.openlca.simapro.csv.model.process.ProductOutputRow;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;
import org.openlca.simapro.csv.model.refdata.Quantity;
import org.openlca.simapro.csv.model.refdata.UnitRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessConverter {

	private Logger log = LoggerFactory.getLogger(getClass());
	private SPProcess spProcess;
	private Process process;
	private SPReferenceData referenceData;
	private Map<String, CSVGeographyContent> geoMap;
	private Map<String, SPElementaryFlowContent> elemMap;
	private Map<String, CSVElementaryCategoryContent> categoryMap;
	private Map<String, CSVUnitContent> unitMap;
	private Map<String, CSVQuantityContent> quantityMap;
	private UnitMapping unitMapping;

	ProcessConverter(IDatabase database, SPReferenceData referenceData) {
		this.referenceData = referenceData;
		unitMapping = UnitMapping.createDefault(database);
		MappingBuilder mappingBuilder = new MappingBuilder(database);
		geoMap = mappingBuilder.buildExportMapping(CSVGeographyContent.class,
				MapType.CSV_GEOGRAPHY);
		elemMap = mappingBuilder.buildExportMapping(
				SPElementaryFlowContent.class, MapType.CSV_ELEMENTARY_FLOW);
		categoryMap = mappingBuilder.buildExportMapping(
				CSVElementaryCategoryContent.class,
				MapType.CSV_ELEMENTARY_CATEGORY);
		unitMap = mappingBuilder.buildExportMapping(CSVUnitContent.class,
				MapType.CSV_UNIT);
		quantityMap = mappingBuilder.buildExportMapping(
				CSVQuantityContent.class, MapType.CSV_QUANTITY);
	}

	SPProcess convert(Process process) {
		this.process = process;
		spProcess = new SPProcess(referenceProduct());
		generalInformations();
		elementaryExchanges();
		productExchanges();
		literatureReferences();
		return spProcess;
	}

	private void generalInformations() {
		SPProcessDocumentation documentation = new SPProcessDocumentation(
				process.getName(), ProcessCategory.MATERIAL, getType());
		spProcess.setDocumentation(documentation);
		documentation.setComment(process.getDescription());
		documentation.setCreationDate(process.getDocumentation()
				.getCreationDate().toString());
		documentation.setInfrastructureProcess(process
				.isInfrastructureProcess());
		geography(documentation);

		System.out.println(documentation.getGenerator());
		if (documentation.getGenerator() != null)
			documentation.setGenerator(documentation.getGenerator());
	}

	private String getActorAsString(Actor actor) {
		StringBuilder builder = new StringBuilder();
		builder.append("Name: " + actor.getName());
		if (actor.getAddress() != null)
			builder.append(" Address:" + actor.getAddress());
		if (actor.getCity() != null)
			builder.append(" City:" + actor.getCity());
		if (actor.getCountry() != null)
			builder.append(" e-mail: " + actor.getEmail());
		if (actor.getTelefax() != null)
			builder.append(" Telefax: " + actor.getTelefax());
		if (actor.getTelephone() != null)
			builder.append(" Telephone: " + actor.getTelephone());
		if (actor.getWebsite() != null)
			builder.append(" Website: " + actor.getWebsite());
		if (actor.getZipCode() != null)
			builder.append(" Zip code: " + actor.getZipCode());
		return builder.toString();
	}

	private ProcessType getType() {
		if (process.getProcessType() == org.openlca.core.model.ProcessType.UNIT_PROCESS)
			return ProcessType.UNIT_PROCESS;
		return ProcessType.SYSTEM;
	}

	private void geography(SPProcessDocumentation documentation) {
		CSVGeographyContent content = geoMap.get(process.getLocation()
				.getRefId());
		if (content != null)
			documentation.setGeography(content.getGeography());
		else
			documentation.setGeography(Geography.UNKNOWN);
	}

	private void literatureReferences() {
		for (Source source : process.getDocumentation().getSources()) {
			String category = null;
			if (source.getCategory() != null)
				category = source.getCategory().getName();
			else
				category = "Others";
			LiteratureReferenceBlock reference = new LiteratureReferenceBlock(
					source.getName(), source.getTextReference(), category);
			if (!"".equals(source.getDescription()))
				reference.setContent(source.getDescription());
			referenceData.add(source.getName(), reference);
			spProcess.getDocumentation().getLiteratureReferenceEntries()
					.add(new LiteratureReferenceRow(reference));
		}
	}

	private ProductOutputRow referenceProduct() {
		Exchange exchange = process.getQuantitativeReference();
		ProductOutputRow product = new ProductOutputRow();
		product.setName(exchange.getFlow().getName());
		product.setUnit(map(exchange.getUnit()).getName());
		product.setAmount(String.valueOf(exchange.getAmountValue()));
		// TODO create a category tree
		// product.setCategory(process.getCategory().getName());
		product.setCategory("Others");

		// TODO: check
		return product;
	}

	private void productExchanges() {
		for (Exchange exchange : process.getExchanges()) {
			if (exchange.getFlow().getFlowType() != FlowType.PRODUCT_FLOW
					|| exchange.getFlow().getFlowType() != FlowType.WASTE_FLOW)
				continue;
			Flow olcaFlow = exchange.getFlow();
			ProductExchangeRow productFlow = new ProductExchangeRow();
			productFlow.setName(olcaFlow.getName());
			productFlow.setUnit(map(exchange.getUnit()).getName());
			productFlow.setAmount(String.valueOf(exchange.getAmountValue()));
			productFlow.setType(getProductFlowType(exchange));
			spProcess.getProductFlows().add(productFlow);
		}
	}

	private ProductType getProductFlowType(Exchange exchange) {
		if (exchange.isInput())
			return ProductType.MATERIAL_INPUT;
		else
			return ProductType.AVOIDED_PRODUCT;
	}

	private void elementaryExchanges() {
		for (Exchange exchange : process.getExchanges()) {
			if (exchange.getFlow().getFlowType() != FlowType.ELEMENTARY_FLOW)
				continue;
			String casNumber = null;
			ElementaryExchangeRow flow = null;
			SPElementaryFlowContent content = elemMap.get(exchange.getFlow()
					.getRefId());
			if (content != null) {
				flow = content.createFlow();
				referenceData.add(content.getUnit());
				referenceData.add(new Quantity(content.getUnit()
						.getQuantity(), new UnitRow(content.getUnit()
						.getReferenceUnit())));
				casNumber = content.getCasNumber();
			} else {
				flow = createElementaryFlow(exchange);
				casNumber = exchange.getFlow().getCasNumber();
			}
			if (exchange.isInput()) {
				flow.setType(ElementaryFlowType.RESOURCE);
			} else {
				if (flow.getType() != null) {
					// TODO: throw right exception
				}
				// TODO: on this point the conversion will failed.
			}
			flow.setAmount(String.valueOf(exchange.getAmountValue()));
			flow.setUncertainty(convertDistribition(exchange.getUncertainty(),
					exchange.getPedigreeUncertainty()));
			ElementaryFlowRow substance = new ElementaryFlowRow(flow.getName(),
					flow.getUnit());
			substance.setFlowType(flow.getType());
			substance.setCASNumber(casNumber);
			referenceData.add(substance);
			spProcess.getElementaryFlows().add(flow);
		}
	}

	private ElementaryExchangeRow createElementaryFlow(Exchange exchange) {
		Flow olcaFlow = exchange.getFlow();
		ElementaryExchangeRow flow = new ElementaryExchangeRow();
		flow.setName(olcaFlow.getName());
		flow.setUnit(map(exchange.getUnit()).getName());
		flow.setAmount(String.valueOf(exchange.getAmountValue()));
		CSVElementaryCategoryContent categoryContent = categoryMap.get(olcaFlow
				.getCategory().getRefId());
		if (categoryContent != null) {
			flow.setType(categoryContent.getType());
			flow.setSubCompartment(categoryContent.getSubCompartment());
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append("Can not find category mapping for flow '");
			builder.append(olcaFlow.getName());
			builder.append("' in category '");
			Category current = olcaFlow.getCategory();
			String categoryTree = null;
			while (current != null) {
				if (categoryTree != null)
					categoryTree = "/" + categoryTree;
				categoryTree = current.getName() + categoryTree;
				current = current.getCategory();
			}
			builder.append(categoryTree);
			log.error(builder.toString());
		}
		return flow;
	}

	private UnitRow map(Unit unit) {
		CSVUnitContent content = unitMap.get(unit.getRefId());
		UnitRow spUnit = null;
		if (content == null) {
			spUnit = new UnitRow(unit.getName());
			spUnit.setConversionFactor(unit.getConversionFactor());
			UnitGroup unitGroup = unitMapping.getUnitGroup(unit.getName());
			spUnit.setQuantity(map(unitGroup).getName());
			spUnit.setReferenceUnit(unitGroup.getReferenceUnit().getName());
		} else {
			spUnit = content.createUnit();
		}
		referenceData.add(spUnit);
		referenceData.add(new Quantity(spUnit.getQuantity(), new UnitRow(
				spUnit.getReferenceUnit())));
		return spUnit;
	}

	private Quantity map(UnitGroup unitGroup) {
		CSVQuantityContent content = quantityMap.get(unitGroup.getRefId());
		if (content == null)
			return new Quantity(unitGroup.getName(), new UnitRow(unitGroup
					.getReferenceUnit().getName(), unitGroup.getReferenceUnit()
					.getConversionFactor()));
		return content.getQuantity();
	}

	private IDistribution convertDistribition(Uncertainty u,
			String pedigreeMatrix) {
		if (u == null || u.getDistributionType() == null)
			return null;
		switch (u.getDistributionType()) {
		case LOG_NORMAL:
			return new SPLogNormalDistribution(u.getParameter2Value(),
					convertPedigreeMatrix(pedigreeMatrix));
		case NORMAL:
			return new SPNormalDistribution(u.getParameter2Value());
		case TRIANGLE:
			return new SPTriangleDistribution(u.getParameter1Value(),
					u.getParameter3Value());
		case UNIFORM:
			return new SPUniformDistribution(u.getParameter1Value(),
					u.getParameter2Value());
		default:
			return null;
		}
	}

	private SPPedigreeMatrix convertPedigreeMatrix(String pedigreeMatrix) {
		if (pedigreeMatrix == null)
			return null;
		SPPedigreeMatrix spPedigreeMatrix = new SPPedigreeMatrix();
		Map<PedigreeMatrixRow, Integer> bla = PedigreeMatrix
				.fromString(pedigreeMatrix);
		for (Map.Entry<PedigreeMatrixRow, Integer> entry : bla.entrySet()) {
			switch (entry.getKey()) {
			case COMPLETENESS:
				spPedigreeMatrix.setCompleteness(String.valueOf(entry
						.getValue()));
				break;
			case GEOGRAPHY:
				spPedigreeMatrix.setGeographicalCorrelation(String
						.valueOf(entry.getValue()));
				break;
			case RELIABILITY:
				spPedigreeMatrix
						.setReliability(String.valueOf(entry.getValue()));
				break;
			case TECHNOLOGY:
				spPedigreeMatrix.setTechnologicalCorrelation(String
						.valueOf(entry.getValue()));
				break;
			case TIME:
				spPedigreeMatrix.setTemporalCorrelation(String.valueOf(entry
						.getValue()));
				break;
			}
		}
		return spPedigreeMatrix;
	}
}
