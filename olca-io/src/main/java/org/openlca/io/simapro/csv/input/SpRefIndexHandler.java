package org.openlca.io.simapro.csv.input;

import org.openlca.simapro.csv.model.annotations.BlockHandler;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.process.ExchangeRow;
import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.openlca.simapro.csv.model.process.ProductExchangeRow;
import org.openlca.simapro.csv.model.process.ProductOutputRow;
import org.openlca.simapro.csv.model.refdata.AirEmissionBlock;
import org.openlca.simapro.csv.model.refdata.CalculatedParameterBlock;
import org.openlca.simapro.csv.model.refdata.DatabaseCalculatedParameterBlock;
import org.openlca.simapro.csv.model.refdata.DatabaseInputParameterBlock;
import org.openlca.simapro.csv.model.refdata.EconomicIssueBlock;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.model.refdata.FinalWasteFlowBlock;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowBlock;
import org.openlca.simapro.csv.model.refdata.InputParameterBlock;
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
		for (UnitRow unitRow : block.units) {
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
	public void handleElementaryFlows(ElementaryFlowBlock block) {
		for (ElementaryFlowRow row : block.rows()) {
			index.put(row, block.type());
		}
	}

	@BlockHandler(subTypes = {
			DatabaseInputParameterBlock.class,
			ProjectInputParameterBlock.class })
	public void handleInputParameters(InputParameterBlock block) {
		index.putInputParameters(block.rows());
	}

	@BlockHandler(subTypes = {
			DatabaseCalculatedParameterBlock.class,
			ProjectCalculatedParameterBlock.class })
	public void handleCalculatedParameters(CalculatedParameterBlock block) {
		index.putCalculatedParameters(block.rows());
	}

	@BlockHandler
	public void handleProcesses(ProcessBlock block) {
		for (ProductOutputRow row : block.products)
			indexProduct(row);
		if (block.wasteTreatment != null)
			indexProduct(block.wasteTreatment);
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
		index.putUsedUnit(row.unit);
		index.putElemFlow(row, type);
	}

	private void indexProduct(ExchangeRow row) {
		index.putUsedUnit(row.unit);
		index.putProduct(row);
	}

}
