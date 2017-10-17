package org.openlca.io.simapro.csv.input;

import java.util.List;

import org.openlca.simapro.csv.model.AbstractExchangeRow;
import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.model.annotations.BlockHandler;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.openlca.simapro.csv.model.process.ProductExchangeRow;
import org.openlca.simapro.csv.model.process.ProductOutputRow;
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
import org.openlca.simapro.csv.model.refdata.QuantityBlock;
import org.openlca.simapro.csv.model.refdata.QuantityRow;
import org.openlca.simapro.csv.model.refdata.RawMaterialBlock;
import org.openlca.simapro.csv.model.refdata.SocialIssueBlock;
import org.openlca.simapro.csv.model.refdata.SoilEmissionBlock;
import org.openlca.simapro.csv.model.refdata.UnitBlock;
import org.openlca.simapro.csv.model.refdata.UnitRow;
import org.openlca.simapro.csv.model.refdata.WaterEmissionBlock;

/**
 * An event handler that fills a SPRefDataIndex when parsing a SimaPro CSV file.
 */
class SpRefIndexHandler {

	private final SpRefDataIndex index;

	public SpRefIndexHandler() {
		index = new SpRefDataIndex();
	}

	public SpRefDataIndex getIndex() {
		return index;
	}

	@BlockHandler
	public void handleQuantities(QuantityBlock block) {
		for (QuantityRow quantity : block.getQuantities())
			index.put(quantity);
	}

	@BlockHandler
	public void handleUnits(UnitBlock block) {
		for (UnitRow unitRow : block.getUnits()) {
			index.put(unitRow);
		}
	}

	@BlockHandler
	public void handleLiteratureRef(LiteratureReferenceBlock block) {
		index.put(block);
	}

	@BlockHandler(subTypes = { AirEmissionBlock.class,
			EconomicIssueBlock.class, FinalWasteFlowBlock.class,
			NonMaterialEmissionBlock.class, RawMaterialBlock.class,
			SocialIssueBlock.class, SoilEmissionBlock.class,
			WaterEmissionBlock.class })
	public void handleElementaryFlows(IElementaryFlowBlock block) {
		for (ElementaryFlowRow row : block.getFlows()) {
			index.put(row, block.getFlowType());
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
			index.putInputParameters(params);
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
			index.putCalculatedParameters(params);
	}

	@BlockHandler
	public void handleProcesses(ProcessBlock block) {
		for (ProductOutputRow row : block.getProducts())
			indexProduct(row);
		if (block.getWasteTreatment() != null)
			indexProduct(block.getWasteTreatment());
		for (ProductType type : ProductType.values()) {
			for (ProductExchangeRow row : block.getProductExchanges(type)) {
				indexProduct(row);
				index.putProductType(row, type);
			}
		}
		for (ElementaryFlowType type : ElementaryFlowType.values()) {
			for (ElementaryExchangeRow row : block
					.getElementaryExchangeRows(type))
				indexElemFlow(row, type);
		}
	}

	private void indexElemFlow(ElementaryExchangeRow row,
			ElementaryFlowType type) {
		index.putUsedUnit(row.getUnit());
		index.putElemFlow(row, type);
	}

	private void indexProduct(AbstractExchangeRow row) {
		index.putUsedUnit(row.getUnit());
		index.putProduct(row);
	}

}
