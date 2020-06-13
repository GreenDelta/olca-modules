package org.openlca.simapro.csv;

import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.openlca.simapro.csv.model.refdata.IElementaryFlowBlock;
import org.openlca.simapro.csv.model.refdata.IParameterBlock;
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
	 * Handles global input or calculated parameters.
	 */
	public void parameters(IParameterBlock block) {
	}

	public void process(ProcessBlock block) {
	}

	public void quantities(QuantityBlock block) {
	}

	public void units(UnitBlock block) {
	}

}
