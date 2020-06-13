package org.openlca.simapro.csv;

import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.openlca.simapro.csv.model.refdata.CalculatedParameterBlock;
import org.openlca.simapro.csv.model.refdata.IElementaryFlowBlock;
import org.openlca.simapro.csv.model.refdata.IParameterBlock;
import org.openlca.simapro.csv.model.refdata.InputParameterBlock;
import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;
import org.openlca.simapro.csv.model.refdata.QuantityBlock;
import org.openlca.simapro.csv.model.refdata.UnitBlock;

/**
 * The CSV parser extracts blocks from a file and passes them to the respective
 * methods of the block handler.
 */
public abstract class BlockHandler {

	public void elementaryFlows(IElementaryFlowBlock block) {
	}

	public void literature(LiteratureReferenceBlock block) {
	}

	/**
	 * Handles global (database or project) input parameters.
	 */
	public void inputParameters(InputParameterBlock block) {
	}

	/**
	 * Handles global (database or project) calculated parameters.
	 */
	public void calculatedParameters(CalculatedParameterBlock block) {
	}

	public void process(ProcessBlock block) {
	}

	public void quantities(QuantityBlock block) {
	}

	public void units(UnitBlock block) {
	}

}
