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
	public ProcessCategory category;

	@SectionValue("Process identifier")
	public String identifier;

	@SectionValue("Type")
	public ProcessType processType;

	@SectionValue("Process name")
	public String name;

	@SectionValue("Status")
	public Status status;

	@SectionValue("Time period")
	public TimePeriod time;

	@SectionValue("Geography")
	public Geography geography;

	@SectionValue("Technology")
	public Technology technology;

	@SectionValue("Representativeness")
	public Representativeness representativeness;

	@SectionValue("Multiple output allocation")
	public ProcessAllocation allocation;

	@SectionValue("Substitution allocation")
	public Substitution substitution;

	@SectionValue("Cut off rules")
	public CutOffRule cutoff;

	@SectionValue("Capital goods")
	public String capitalGoods;

	@SectionValue("Boundary with nature")
	public BoundaryWithNature boundaryWithNature;

	@SectionValue("Infrastructure")
	public Boolean infrastructure;

	@SectionValue("Date")
	public Date date;

	@SectionValue("Record")
	public String record;

	@SectionValue("Generator")
	public String generator;

	@SectionRows("Literature references")
	public List<LiteratureReferenceRow> literatureReferences = new ArrayList<>();

	@SectionValue("Collection method")
	public String collectionMethod;

	@SectionValue("Verification")
	public String verification;

	@SectionValue("Comment")
	public String comment;

	@SectionValue("Allocation rules")
	public String allocationRules;

	@SectionRow("System description")
	public SystemDescriptionRow systemDescription;

	@SectionValue("Data treatment")
	public String dataTreatment;

	@SectionRows("Products")
	public List<ProductOutputRow> products = new ArrayList<>();

	@SectionRow("Waste treatment")
	public WasteTreatmentRow wasteTreatment;

	@SectionRows("Avoided products")
	public List<ProductExchangeRow> avoidedProducts = new ArrayList<>();

	@SectionRows("Materials/fuels")
	public List<ProductExchangeRow> materialsAndFuels = new ArrayList<>();

	@SectionRows("Electricity/heat")
	public List<ProductExchangeRow> electricityAndHeat = new ArrayList<>();

	@SectionRows("Waste to treatment")
	public List<ProductExchangeRow> wasteToTreatment = new ArrayList<>();

	@SectionRows("Resources")
	public List<ElementaryExchangeRow> resources = new ArrayList<>();

	@SectionRows("Emissions to air")
	public List<ElementaryExchangeRow> emissionsToAir = new ArrayList<>();

	@SectionRows("Emissions to water")
	public List<ElementaryExchangeRow> emissionsToWater = new ArrayList<>();

	@SectionRows("Emissions to soil")
	public List<ElementaryExchangeRow> emissionsToSoil = new ArrayList<>();

	@SectionRows("Final waste flows")
	public List<ElementaryExchangeRow> finalWasteFlows = new ArrayList<>();

	@SectionRows("Non material emissions")
	public List<ElementaryExchangeRow> nonMaterialEmissions = new ArrayList<>();

	@SectionRows("Social issues")
	public List<ElementaryExchangeRow> socialIssues = new ArrayList<>();

	@SectionRows("Economic issues")
	public List<ElementaryExchangeRow> economicIssues = new ArrayList<>();

	@SectionRows("Input parameters")
	public List<InputParameterRow> inputParameters = new ArrayList<>();

	@SectionRows("Calculated parameters")
	public List<CalculatedParameterRow> calculatedParameters = new ArrayList<>();

	public List<ElementaryExchangeRow> getElementaryExchangeRows(
			ElementaryFlowType type) {
		if (type == null)
			return Collections.emptyList();
		return switch (type) {
			case ECONOMIC_ISSUES -> economicIssues;
			case EMISSIONS_TO_AIR -> emissionsToAir;
			case EMISSIONS_TO_SOIL -> emissionsToSoil;
			case EMISSIONS_TO_WATER -> emissionsToWater;
			case FINAL_WASTE_FLOWS -> finalWasteFlows;
			case NON_MATERIAL_EMISSIONS -> nonMaterialEmissions;
			case RESOURCES -> resources;
			case SOCIAL_ISSUES -> socialIssues;
		};
	}

	public List<ProductExchangeRow> getProductExchanges(ProductType productType) {
		if (productType == null)
			return Collections.emptyList();
		return switch (productType) {
			case AVOIDED_PRODUCTS -> avoidedProducts;
			case ELECTRICITY_HEAT -> electricityAndHeat;
			case MATERIAL_FUELS -> materialsAndFuels;
			case WASTE_TO_TREATMENT -> wasteToTreatment;
		};
	}
}
