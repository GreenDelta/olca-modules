package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.ProductType;
import org.openlca.simapro.csv.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.process.ExchangeRow;
import org.openlca.simapro.csv.process.RefExchangeRow;
import org.openlca.simapro.csv.process.TechExchangeRow;
import org.openlca.simapro.csv.refdata.CalculatedParameterRow;
import org.openlca.simapro.csv.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.refdata.InputParameterRow;
import org.openlca.simapro.csv.refdata.LiteratureReferenceBlock;
import org.openlca.simapro.csv.refdata.QuantityRow;
import org.openlca.simapro.csv.refdata.UnitRow;
import org.openlca.util.KeyGen;

/**
 * Contains all reference data from a SimaPro CSV file.
 */
class SpRefDataIndex {

	private final HashMap<String, QuantityRow> quantities = new HashMap<>();
	private HashSet<String> usedUnits = new HashSet<>();
	private final HashMap<String, UnitRow> unitRows = new HashMap<>();
	private final HashMap<String, LiteratureReferenceBlock> sources = new HashMap<>();
	private final HashMap<String, ElementaryFlowRow> elemFlowInfos = new HashMap<>();
	private final List<InputParameterRow> inputParameters = new ArrayList<>();
	private final List<CalculatedParameterRow> calculatedParameters = new ArrayList<>();
	private final HashMap<String, ExchangeRow> products = new HashMap<>();
	private final HashMap<String, ProductType> productTypes = new HashMap<>();
	private final HashMap<ElementaryFlowType, HashMap<String, ElementaryExchangeRow>> elemFlows = new HashMap<>();

	void put(QuantityRow quantity) {
		if (quantity == null)
			return;
		quantities.put(quantity.name(), quantity);
	}

	QuantityRow getQuantity(String name) {
		return quantities.get(name);
	}

	void put(UnitRow unitRow) {
		if (unitRow == null)
			return;
		String name = unitRow.name();
		unitRows.put(name, unitRow);
	}

	UnitRow getUnitRow(String name) {
		return unitRows.get(name);
	}

	Collection<UnitRow> getUnitRows() {
		return unitRows.values();
	}

	void putUsedUnit(String unitName) {
		if (unitName != null)
			usedUnits.add(unitName);
	}

	Set<String> getUsedUnits() {
		return usedUnits;
	}

	void put(LiteratureReferenceBlock reference) {
		if (reference == null)
			return;
		sources.put(reference.name(), reference);
	}

	Collection<LiteratureReferenceBlock> getLiteratureReferences() {
		return sources.values();
	}

	void put(ElementaryFlowRow elemFlowRow, ElementaryFlowType type) {
		if (elemFlowRow == null || type == null)
			return;
		String key = KeyGen.get(elemFlowRow.name(), type.exchangeHeader());
		elemFlowInfos.put(key, elemFlowRow);
	}

	ElementaryFlowRow getFlowInfo(String name, ElementaryFlowType type) {
		String key = KeyGen.get(name, type.exchangeHeader());
		return elemFlowInfos.get(key);
	}

	void putInputParameters(List<InputParameterRow> params) {
		if (params == null)
			return;
		inputParameters.addAll(params);
	}

	List<InputParameterRow> getInputParameters() {
		return inputParameters;
	}

	void putCalculatedParameters(List<CalculatedParameterRow> params) {
		if (params == null)
			return;
		calculatedParameters.addAll(params);
	}

	List<CalculatedParameterRow> getCalculatedParameters() {
		return calculatedParameters;
	}

	void putProduct(ExchangeRow row) {
		if (row == null)
			return;
		String key = row.name();
		ExchangeRow existingRow = products.get(key);
		// favour reference product rows
		if (existingRow == null || (row instanceof RefExchangeRow))
			products.put(row.name(), row);
	}

	Collection<ExchangeRow> getProducts() {
		return products.values();
	}

	void putProductType(TechExchangeRow row, ProductType type) {
		if (row == null || type == null)
			return;
		productTypes.put(row.name(), type);
	}

	ProductType getProductType(TechExchangeRow row) {
		if (row == null)
			return null;
		return productTypes.get(row.name());
	}

	void putElemFlow(ElementaryExchangeRow row, ElementaryFlowType type) {
		if (row == null || type == null)
			return;
		var map = elemFlows.computeIfAbsent(type, k -> new HashMap<>());
		String key = Flows.getMappingID(type, row);
		map.put(key, row);
	}

	Collection<ElementaryExchangeRow> getElementaryFlows(
		ElementaryFlowType type) {
		if (type == null)
			return Collections.emptyList();
		var rows = elemFlows.get(type);
		return rows == null
			? Collections.emptyList()
			: rows.values();
	}

}
