package org.openlca.io.simapro.csv.input;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Source;
import org.openlca.io.KeyGen;
import org.openlca.io.UnitMapping;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.process.LiteratureReferenceRow;
import org.openlca.simapro.csv.model.process.ProductExchangeRow;
import org.openlca.simapro.csv.model.process.RefProductRow;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;

import java.util.HashMap;

class RefData {

	private UnitMapping unitMapping;
	private HashMap<String, ElementaryFlowRow> elementaryFlowInfos = new HashMap<>();
	private HashMap<String, Flow> productFlows = new HashMap<>();
	private HashMap<String, Flow> elementaryFlows = new HashMap<>();
	private HashMap<String, Source> sources = new HashMap<>();

	public void setUnitMapping(UnitMapping unitMapping) {
		this.unitMapping = unitMapping;
	}

	public UnitMapping getUnitMapping() {
		return unitMapping;
	}

	void put(ElementaryFlowRow row, ElementaryFlowType type) {
		if (row == null || type == null)
			return;
		String key = KeyGen.get(type.getExchangeHeader(), row.getName());
		elementaryFlowInfos.put(key, row);
	}

	ElementaryFlowRow getFlowInfo(ElementaryExchangeRow exchange,
			ElementaryFlowType type) {
		if (exchange == null || type == null)
			return null;
		String key = KeyGen.get(type.getExchangeHeader(), exchange.getName());
		return elementaryFlowInfos.get(key);
	}

	void put(ElementaryExchangeRow exchange, ElementaryFlowType type, Flow flow) {
		if (exchange == null || type == null || flow == null)
			return;
		String key = KeyGen.get(exchange.getName(), type.getExchangeHeader(),
				exchange.getSubCompartment(), exchange.getUnit());
		elementaryFlows.put(key, flow);
	}

	Flow getFlow(ElementaryExchangeRow exchange, ElementaryFlowType type) {
		if (exchange == null || type == null)
			return null;
		String key = KeyGen.get(exchange.getName(), type.getExchangeHeader(),
				exchange.getSubCompartment(), exchange.getUnit());
		return elementaryFlows.get(key);
	}

	void put(LiteratureReferenceBlock block, Source source) {
		if (block == null || source == null)
			return;
		sources.put(block.getName(), source);
	}

	Source getSource(LiteratureReferenceRow row) {
		if (row == null)
			return null;
		return sources.get(row.getName());
	}

	void put(RefProductRow row, Flow flow) {
		if (row == null || flow == null)
			return;
		productFlows.put(row.getName(), flow);
	}

	Flow getFlow(RefProductRow row) {
		if (row == null)
			return null;
		return productFlows.get(row.getName());
	}

	Flow getFlow(ProductExchangeRow row) {
		if (row == null)
			return null;
		return productFlows.get(row.getName());
	}

	void put(ProductExchangeRow row, Flow flow) {
		if (row != null && flow != null)
			productFlows.put(row.getName(), flow);
	}
}
