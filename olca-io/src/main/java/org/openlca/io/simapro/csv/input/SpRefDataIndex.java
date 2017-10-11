package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.simapro.csv.model.AbstractExchangeRow;
import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.process.ProductExchangeRow;
import org.openlca.simapro.csv.model.process.RefProductRow;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;
import org.openlca.simapro.csv.model.refdata.QuantityRow;
import org.openlca.simapro.csv.model.refdata.UnitRow;
import org.openlca.util.KeyGen;

/**
 * Contains all reference data from a SimaPro CSV file.
 */
class SpRefDataIndex {

	private HashMap<String, QuantityRow> quantities = new HashMap<>();
	private HashSet<String> usedUnits = new HashSet<>();
	private HashMap<String, UnitRow> unitRows = new HashMap<>();
	private HashMap<String, LiteratureReferenceBlock> literatureReferences = new HashMap<>();
	private HashMap<String, ElementaryFlowRow> elemFlowInfos = new HashMap<>();
	private List<InputParameterRow> inputParameters = new ArrayList<>();
	private List<CalculatedParameterRow> calculatedParameters = new ArrayList<>();
	private HashMap<String, AbstractExchangeRow> products = new HashMap<>();
	private HashMap<String, ProductType> productTypes = new HashMap<>();
	private HashMap<ElementaryFlowType, HashMap<String, ElementaryExchangeRow>> elemFlows = new HashMap<>();

	public void put(QuantityRow quantity) {
		if (quantity == null)
			return;
		quantities.put(quantity.getName(), quantity);
	}

	public QuantityRow getQuantity(String name) {
		return quantities.get(name);
	}

	public void put(UnitRow unitRow) {
		if (unitRow == null)
			return;
		String name = unitRow.getName();
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
		literatureReferences.put(reference.getName(), reference);
	}

	public Collection<LiteratureReferenceBlock> getLiteratureReferences() {
		return literatureReferences.values();
	}

	public void put(ElementaryFlowRow elemFlowRow, ElementaryFlowType type) {
		if (elemFlowRow == null || type == null)
			return;
		String key = KeyGen
				.get(elemFlowRow.getName(), type.getExchangeHeader());
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

	public void putProduct(AbstractExchangeRow row) {
		if (row == null)
			return;
		String key = row.getName();
		AbstractExchangeRow existingRow = products.get(key);
		// favour reference product rows
		if (existingRow == null || (row instanceof RefProductRow))
			products.put(row.getName(), row);
	}

	public Collection<AbstractExchangeRow> getProducts() {
		return products.values();
	}

	public void putProductType(ProductExchangeRow row, ProductType type) {
		if (row == null || type == null)
			return;
		productTypes.put(row.getName(), type);
	}

	public ProductType getProductType(ProductExchangeRow row) {
		if (row == null)
			return null;
		return productTypes.get(row.getName());
	}

	public void putElemFlow(ElementaryExchangeRow row, ElementaryFlowType type) {
		if (row == null || type == null)
			return;
		HashMap<String, ElementaryExchangeRow> map = elemFlows.get(type);
		if (map == null) {
			map = new HashMap<>();
			elemFlows.put(type, map);
		}
		String key = KeyGen.get(row.getName(), type.getExchangeHeader(),
				row.getSubCompartment(), row.getUnit());
		map.put(key, row);
	}

	public Collection<ElementaryExchangeRow> getElementaryFlows(
			ElementaryFlowType type) {
		if (type == null)
			return Collections.emptyList();
		HashMap<String, ElementaryExchangeRow> rows = elemFlows.get(type);
		if (rows == null)
			return Collections.emptyList();
		else
			return rows.values();
	}

}
