package org.openlca.simapro.csv.model.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.SectionRow;
import org.openlca.simapro.csv.model.annotations.SectionRows;
import org.openlca.simapro.csv.model.annotations.SectionValue;
import org.openlca.simapro.csv.model.enums.BoundaryWithNature;
import org.openlca.simapro.csv.model.enums.CutOffRule;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.Geography;
import org.openlca.simapro.csv.model.enums.ProcessAllocation;
import org.openlca.simapro.csv.model.enums.ProcessCategory;
import org.openlca.simapro.csv.model.enums.ProcessType;
import org.openlca.simapro.csv.model.enums.ProductType;
import org.openlca.simapro.csv.model.enums.Representativeness;
import org.openlca.simapro.csv.model.enums.Status;
import org.openlca.simapro.csv.model.enums.Substitution;
import org.openlca.simapro.csv.model.enums.Technology;
import org.openlca.simapro.csv.model.enums.TimePeriod;

@BlockModel("Process")
public class ProcessBlock {

	@SectionValue("Category type")
	private ProcessCategory category;

	@SectionValue("Process identifier")
	private String identifier;

	@SectionValue("Type")
	private ProcessType processType;

	@SectionValue("Process name")
	private String name;

	@SectionValue("Status")
	private Status status;

	@SectionValue("Time period")
	private TimePeriod time;

	@SectionValue("Geography")
	private Geography geography;

	@SectionValue("Technology")
	private Technology technology;

	@SectionValue("Representativeness")
	private Representativeness representativeness;

	@SectionValue("Multiple output allocation")
	private ProcessAllocation allocation;

	@SectionValue("Substitution allocation")
	private Substitution substitution;

	@SectionValue("Cut off rules")
	private CutOffRule cutoff;

	@SectionValue("Capital goods")
	private String capitalGoods;

	@SectionValue("Boundary with nature")
	private BoundaryWithNature boundaryWithNature;

	@SectionValue("Infrastructure")
	private Boolean infrastructure;

	@SectionValue("Date")
	private Date date;

	@SectionValue("Record")
	private String record;

	@SectionValue("Generator")
	private String generator;

	@SectionRows("Literature references")
	private List<LiteratureReferenceRow> literatureReferences = new ArrayList<>();

	@SectionValue("Collection method")
	private String collectionMethod;

	@SectionValue("Verification")
	private String verification;

	@SectionValue("Comment")
	private String comment;

	@SectionValue("Allocation rules")
	private String allocationRules;

	@SectionRow("System description")
	private SystemDescriptionRow systemDescription;

	@SectionValue("Data treatment")
	private String dataTreatment;

	@SectionRows("Products")
	private List<ProductOutputRow> products = new ArrayList<>();

	@SectionRow("Waste treatment")
	private WasteTreatmentRow wasteTreatment;

	@SectionRows("Avoided products")
	private List<ProductExchangeRow> avoidedProducts = new ArrayList<>();

	@SectionRows("Materials/fuels")
	private List<ProductExchangeRow> materialsAndFuels = new ArrayList<>();

	@SectionRows("Electricity/heat")
	private List<ProductExchangeRow> electricityAndHeat = new ArrayList<>();

	@SectionRows("Waste to treatment")
	private List<ProductExchangeRow> wasteToTreatment = new ArrayList<>();

	@SectionRows("Resources")
	private List<ElementaryExchangeRow> resources = new ArrayList<>();

	@SectionRows("Emissions to air")
	private List<ElementaryExchangeRow> emissionsToAir = new ArrayList<>();

	@SectionRows("Emissions to water")
	private List<ElementaryExchangeRow> emissionsToWater = new ArrayList<>();

	@SectionRows("Emissions to soil")
	private List<ElementaryExchangeRow> emissionsToSoil = new ArrayList<>();

	@SectionRows("Final waste flows")
	private List<ElementaryExchangeRow> finalWasteFlows = new ArrayList<>();

	@SectionRows("Non material emissions")
	private List<ElementaryExchangeRow> nonMaterialEmissions = new ArrayList<>();

	@SectionRows("Social issues")
	private List<ElementaryExchangeRow> socialIssues = new ArrayList<>();

	@SectionRows("Economic issues")
	private List<ElementaryExchangeRow> economicIssues = new ArrayList<>();

	@SectionRows("Input parameters")
	private List<InputParameterRow> inputParameters = new ArrayList<>();

	@SectionRows("Calculated parameters")
	private List<CalculatedParameterRow> calculatedParameters = new ArrayList<>();

	public List<ProductOutputRow> getProducts() {
		return products;
	}

	public WasteTreatmentRow getWasteTreatment() {
		return wasteTreatment;
	}

	public void setWasteTreatment(WasteTreatmentRow wasteTreatment) {
		this.wasteTreatment = wasteTreatment;
	}

	public List<ElementaryExchangeRow> getResources() {
		return resources;
	}

	public List<ElementaryExchangeRow> getEmissionsToAir() {
		return emissionsToAir;
	}

	public List<ElementaryExchangeRow> getEmissionsToWater() {
		return emissionsToWater;
	}

	public List<ElementaryExchangeRow> getEmissionsToSoil() {
		return emissionsToSoil;
	}

	public List<ElementaryExchangeRow> getFinalWasteFlows() {
		return finalWasteFlows;
	}

	public List<ElementaryExchangeRow> getNonMaterialEmissions() {
		return nonMaterialEmissions;
	}

	public List<ElementaryExchangeRow> getSocialIssues() {
		return socialIssues;
	}

	public List<ElementaryExchangeRow> getEconomicIssues() {
		return economicIssues;
	}

	public List<ElementaryExchangeRow> getElementaryExchangeRows(
			ElementaryFlowType type) {
		if (type == null)
			return Collections.emptyList();
		switch (type) {
		case ECONOMIC_ISSUES:
			return getEconomicIssues();
		case EMISSIONS_TO_AIR:
			return getEmissionsToAir();
		case EMISSIONS_TO_SOIL:
			return getEmissionsToSoil();
		case EMISSIONS_TO_WATER:
			return getEmissionsToWater();
		case FINAL_WASTE_FLOWS:
			return getFinalWasteFlows();
		case NON_MATERIAL_EMISSIONS:
			return getNonMaterialEmissions();
		case RESOURCES:
			return getResources();
		case SOCIAL_ISSUES:
			return getSocialIssues();
		default:
			return Collections.emptyList();
		}
	}

	public List<ProductExchangeRow> getAvoidedProducts() {
		return avoidedProducts;
	}

	public List<ProductExchangeRow> getMaterialsAndFuels() {
		return materialsAndFuels;
	}

	public List<ProductExchangeRow> getElectricityAndHeat() {
		return electricityAndHeat;
	}

	public List<ProductExchangeRow> getWasteToTreatment() {
		return wasteToTreatment;
	}

	public List<ProductExchangeRow> getProductExchanges(ProductType productType) {
		if (productType == null)
			return Collections.emptyList();
		switch (productType) {
		case AVOIDED_PRODUCTS:
			return getAvoidedProducts();
		case ELECTRICITY_HEAT:
			return getElectricityAndHeat();
		case MATERIAL_FUELS:
			return getMaterialsAndFuels();
		case WASTE_TO_TREATMENT:
			return getWasteToTreatment();
		default:
			return Collections.emptyList();
		}
	}

	public ProcessCategory getCategory() {
		return category;
	}

	public void setCategory(ProcessCategory category) {
		this.category = category;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public ProcessType getProcessType() {
		return processType;
	}

	public void setProcessType(ProcessType processType) {
		this.processType = processType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public TimePeriod getTime() {
		return time;
	}

	public void setTime(TimePeriod time) {
		this.time = time;
	}

	public Geography getGeography() {
		return geography;
	}

	public void setGeography(Geography geography) {
		this.geography = geography;
	}

	public Technology getTechnology() {
		return technology;
	}

	public void setTechnology(Technology technology) {
		this.technology = technology;
	}

	public Representativeness getRepresentativeness() {
		return representativeness;
	}

	public void setRepresentativeness(Representativeness representativeness) {
		this.representativeness = representativeness;
	}

	public ProcessAllocation getAllocation() {
		return allocation;
	}

	public void setAllocation(ProcessAllocation allocation) {
		this.allocation = allocation;
	}

	public Substitution getSubstitution() {
		return substitution;
	}

	public void setSubstitution(Substitution substitution) {
		this.substitution = substitution;
	}

	public CutOffRule getCutoff() {
		return cutoff;
	}

	public void setCutoff(CutOffRule cutoff) {
		this.cutoff = cutoff;
	}

	public String getCapitalGoods() {
		return capitalGoods;
	}

	public void setCapitalGoods(String capitalgoods) {
		this.capitalGoods = capitalgoods;
	}

	public BoundaryWithNature getBoundaryWithNature() {
		return boundaryWithNature;
	}

	public void setBoundaryWithNature(BoundaryWithNature boundarywithnature) {
		this.boundaryWithNature = boundarywithnature;
	}

	public List<InputParameterRow> getInputParameters() {
		return inputParameters;
	}

	public List<CalculatedParameterRow> getCalculatedParameters() {
		return calculatedParameters;
	}

	public Boolean getInfrastructure() {
		return infrastructure;
	}

	public void setInfrastructure(Boolean infrastructure) {
		this.infrastructure = infrastructure;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
	}

	public String getGenerator() {
		return generator;
	}

	public void setGenerator(String generator) {
		this.generator = generator;
	}

	public List<LiteratureReferenceRow> getLiteratureReferences() {
		return literatureReferences;
	}

	public String getCollectionMethod() {
		return collectionMethod;
	}

	public void setCollectionMethod(String collectionMethod) {
		this.collectionMethod = collectionMethod;
	}

	public String getDataTreatment() {
		return dataTreatment;
	}

	public void setDataTreatment(String dataTreatment) {
		this.dataTreatment = dataTreatment;
	}

	public String getVerification() {
		return verification;
	}

	public void setVerification(String verification) {
		this.verification = verification;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getAllocationRules() {
		return allocationRules;
	}

	public void setAllocationRules(String allocationrules) {
		this.allocationRules = allocationrules;
	}

	public SystemDescriptionRow getSystemDescription() {
		return systemDescription;
	}

	public void setSystemDescription(SystemDescriptionRow systemDescription) {
		this.systemDescription = systemDescription;
	}
}
