package org.openlca.io.simapro.csv.input;

import java.util.HashMap;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Source;
import org.openlca.io.KeyGen;
import org.openlca.io.UnitMapping;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.process.LiteratureReferenceRow;
import org.openlca.simapro.csv.model.process.ProductOutputRow;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;

class RefData {

	private UnitMapping unitMapping;
	private HashMap<String, ElementaryFlowRow> elementaryFlows = new HashMap<>();
	private HashMap<String, Flow> productFlows = new HashMap<>();
	private HashMap<String, Source> sources = new HashMap<>();

	public void setUnitMapping(UnitMapping unitMapping) {
		this.unitMapping = unitMapping;
	}

	public UnitMapping getUnitMapping() {
		return unitMapping;
	}

	void put(ElementaryFlowRow row, ElementaryFlowType type) {
		if (row == null)
			return;
		String key = KeyGen.get(type.getExchangeHeader(), row.getName(),
				row.getReferenceUnit());
		elementaryFlows.put(key, row);
	}

	void put(LiteratureReferenceBlock block, Source source) {
		sources.put(block.getName(), source);
	}

	Source getSource(LiteratureReferenceRow row) {
		return sources.get(row.getName());
	}

	void put(ProductOutputRow row, Flow flow) {
		productFlows.put(row.getName(), flow);
	}

	Flow getFlow(ProductOutputRow row) {
		return productFlows.get(row.getName());
	}

}
