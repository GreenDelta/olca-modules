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
import org.openlca.simapro.csv.refdata.CalculatedParameterRow;
import org.openlca.simapro.csv.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.refdata.InputParameterRow;
import org.openlca.simapro.csv.refdata.QuantityRow;
import org.openlca.simapro.csv.refdata.UnitRow;
import org.openlca.util.KeyGen;

/**
 * Contains all reference data from a SimaPro CSV file.
 */
class SpRefDataIndex {

	private HashMap<String, QuantityRow> quantities = new HashMap<>();
	private HashSet<String> usedUnits = new HashSet<>();
	private HashMap<String, UnitRow> unitRows = new HashMap<>();
	private HashMap<String, LiteratureReferenceBlock> sources = new HashMap<>();
	private HashMap<String, ElementaryFlowRow> elemFlowInfos = new HashMap<>();
	private List<InputParameterRow> inputParameters = new ArrayList<>();
	private List<CalculatedParameterRow> calculatedParameters = new ArrayList<>();
	private HashMap<String, ExchangeRow> products = new HashMap<>();
	private HashMap<String, ProductType> productTypes = new HashMap<>();
	private HashMap<ElementaryFlowType, HashMap<String, ElementaryExchangeRow>> elemFlows = new HashMap<>();

	public void put(QuantityRow quantity) {
		if (quantity == null)
			return;
		quantities.put(quantity.name, quantity);
	}

	public QuantityRow getQuantity(String name) {
		return quantities.get(name);
	}

	public void put(UnitRow unitRow) {
		if (unitRow == null)
			return;
		String name = unitRow.name;
		unitRows.put(name, unitRow);
	}

	public UnitRow getUnitRow(String name) {
		return unitRows.get(name);
	}

	public Collection<UnitRow> getUnitRows() {
		return unitRows.values();
	}

	public void putUsedUnit(String unitName) {
		if (unitName != null)
			usedUnits.add(unitName);
	}

	public Set<String> getUsedUnits() {
		return usedUnits;
	}

	public void put(LiteratureReferenceBlock reference) {
		if (reference == null)
			return;
		sources.put(reference.name, reference);
	}

	public Collection<LiteratureReferenceBlock> getLiteratureReferences() {
		return sources.values();
	}

	public void put(ElementaryFlowRow elemFlowRow, ElementaryFlowType type) {
		if (elemFlowRow == null || type == null)
			return;
		String key = KeyGen.get(elemFlowRow.name, type.getExchangeHeader());
		elemFlowInfos.put(key, elemFlowRow);
	}

	public ElementaryFlowRow getFlowInfo(String name, ElementaryFlowType type) {
		String key = KeyGen.get(name, type.getExchangeHeader());
		return elemFlowInfos.get(key);
	}

	public void putInputParameters(List<InputParameterRow> params) {
		if (params == null)
			return;
		inputParameters.addAll(params);
	}

	public List<InputParameterRow> getInputParameters() {
		return inputParameters;
	}

	public void putCalculatedParameters(List<CalculatedParameterRow> params) {
		if (params == null)
			return;
		calculatedParameters.addAll(params);
	}

	public List<CalculatedParameterRow> getCalculatedParameters() {
		return calculatedParameters;
	}

	public void putProduct(ExchangeRow row) {
		if (row == null)
			return;
		String key = row.name;
		ExchangeRow existingRow = products.get(key);
		// favour reference product rows
		if (existingRow == null || (row instanceof RefProductRow))
			products.put(row.name, row);
	}

	public Collection<ExchangeRow> getProducts() {
		return products.values();
	}

	public void putProductType(ProductExchangeRow row, ProductType type) {
		if (row == null || type == null)
			return;
		productTypes.put(row.name, type);
	}

	public ProductType getProductType(ProductExchangeRow row) {
		if (row == null)
			return null;
		return productTypes.get(row.name);
	}

	public void putElemFlow(ElementaryExchangeRow row, ElementaryFlowType type) {
		if (row == null || type == null)
			return;
		var map = elemFlows.computeIfAbsent(type, k -> new HashMap<>());
		String key = Flows.getMappingID(type, row);
		map.put(key, row);
	}

	public Collection<ElementaryExchangeRow> getElementaryFlows(
		ElementaryFlowType type) {
		if (type == null)
			return Collections.emptyList();
		var rows = elemFlows.get(type);
		return rows == null
			? Collections.emptyList()
			: rows.values();
	}

}
