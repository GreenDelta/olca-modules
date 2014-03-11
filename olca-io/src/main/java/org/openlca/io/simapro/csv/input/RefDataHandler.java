package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Source;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;
import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.model.annotations.BlockHandler;
import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.openlca.simapro.csv.model.process.ProductOutputRow;
import org.openlca.simapro.csv.model.process.WasteTreatmentRow;
import org.openlca.simapro.csv.model.refdata.AirEmissionBlock;
import org.openlca.simapro.csv.model.refdata.DatabaseCalculatedParameterBlock;
import org.openlca.simapro.csv.model.refdata.DatabaseInputParameterBlock;
import org.openlca.simapro.csv.model.refdata.EconomicIssueBlock;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.model.refdata.FinalWasteFlowBlock;
import org.openlca.simapro.csv.model.refdata.IElementaryFlowBlock;
import org.openlca.simapro.csv.model.refdata.IParameterBlock;
import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;
import org.openlca.simapro.csv.model.refdata.NonMaterialEmissionBlock;
import org.openlca.simapro.csv.model.refdata.ProjectCalculatedParameterBlock;
import org.openlca.simapro.csv.model.refdata.ProjectInputParameterBlock;
import org.openlca.simapro.csv.model.refdata.Quantity;
import org.openlca.simapro.csv.model.refdata.RawMaterialBlock;
import org.openlca.simapro.csv.model.refdata.SocialIssueBlock;
import org.openlca.simapro.csv.model.refdata.SoilEmissionBlock;
import org.openlca.simapro.csv.model.refdata.UnitBlock;
import org.openlca.simapro.csv.model.refdata.UnitRow;
import org.openlca.simapro.csv.model.refdata.WaterEmissionBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RefDataHandler {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final SimaProCsvImport csvImport;
	private final IDatabase database;
	private RefData refData;

	private List<InputParameterRow> globalInputParamaters = new ArrayList<>();
	private List<CalculatedParameterRow> globalCalculatedParameters = new ArrayList<>();

	public RefDataHandler(SimaProCsvImport csvImport) {
		this.csvImport = csvImport;
		this.database = csvImport.getDatabase();
		this.refData = new RefData();
	}

	public RefData getRefData() {
		return refData;
	}

	@BlockHandler
	public void handleQuantities(Quantity quantity) {
		// TODO: we need a mapping of SimaPro quantities to our flow properties

	}

	@BlockHandler
	public void handleUnits(UnitBlock block) {
		log.trace("map {} units", block.getUnits().size());
		UnitMapping mapping = UnitMapping
				.createDefault(csvImport.getDatabase());
		for (UnitRow unitRow : block.getUnits()) {
			UnitMappingEntry entry = mapping.getEntry(unitRow.getName());
			if (entry == null) {
				log.warn("unknown unit {}; create a new unit group",
						unitRow.getName());
				// TODO: add new units
			}
		}
	}

	@BlockHandler
	public void handleLiteratureRef(LiteratureReferenceBlock block) {
		Source source = new SourceImport(database).run(block);
		if (source != null)
			refData.put(block, source);
	}

	@BlockHandler(subTypes = { AirEmissionBlock.class,
			EconomicIssueBlock.class, FinalWasteFlowBlock.class,
			NonMaterialEmissionBlock.class, RawMaterialBlock.class,
			SocialIssueBlock.class, SoilEmissionBlock.class,
			WaterEmissionBlock.class })
	public void handleElementaryFlows(IElementaryFlowBlock block) {
		for (ElementaryFlowRow row : block.getFlows()) {
			refData.put(row, block.getFlowType());
		}
	}

	@BlockHandler(subTypes = { DatabaseInputParameterBlock.class,
			ProjectInputParameterBlock.class })
	public void handleInputParameters(IParameterBlock block) {
		List<InputParameterRow> params = null;
		if (block instanceof DatabaseInputParameterBlock)
			params = ((DatabaseInputParameterBlock) block).getParameters();
		else if (block instanceof ProjectInputParameterBlock)
			params = ((ProjectInputParameterBlock) block).getParameters();
		if (params != null)
			globalInputParamaters.addAll(params);
	}

	@BlockHandler(subTypes = { DatabaseCalculatedParameterBlock.class,
			ProjectCalculatedParameterBlock.class })
	public void handleCalculatedParameters(IParameterBlock block) {
		List<CalculatedParameterRow> params = null;
		if (block instanceof DatabaseCalculatedParameterBlock)
			params = ((DatabaseCalculatedParameterBlock) block).getParameters();
		else if (block instanceof ProjectCalculatedParameterBlock)
			params = ((ProjectCalculatedParameterBlock) block).getParameters();
		if (params != null)
			globalCalculatedParameters.addAll(params);
	}

	@BlockHandler
	public void handleProducts(ProcessBlock block) {
		FlowHandler flowHandler = new FlowHandler(csvImport.getDatabase());
		for (ProductOutputRow row : block.getProducts()) {
			Flow flow = flowHandler.getProductFlow(row);
			if (flow != null)
				refData.put(row, flow);
		}
		if (block.getWasteTreatment() != null) {
			WasteTreatmentRow row = block.getWasteTreatment();
			Flow flow = flowHandler.getProductFlow(row);
			if (flow != null)
				refData.put(row, flow);
		}
	}

	public void finish() {
		refData.setUnitMapping(UnitMapping.createDefault(database));
		new GlobalParameterImport(database, globalInputParamaters,
				globalCalculatedParameters).run();
	}
}
