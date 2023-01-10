package org.openlca.io.xls.process.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;

class AllocationSheet {

	private final ProcessWorkbook wb;
	private final Process process;
	private final Sheet sheet;

	private AllocationSheet(ProcessWorkbook wb) {
		this.wb = wb;
		this.process = wb.process;
		this.sheet = wb.getSheet("Allocation");
	}

	public static void read(ProcessWorkbook config) {
		new AllocationSheet(config).read();
	}

	private void read() {
		if (sheet == null || process == null)
			return;
		process.defaultAllocationMethod = getMethod(wb.getString(sheet, 0, 1));
		List<Exchange> products = selectProducts();
		if (products.size() <= 1) {
			return;
		}
		readFactors(products);
		readCausalFactors(products);
	}

	private AllocationMethod getMethod(String string) {
		if (string == null)
			return AllocationMethod.NONE;
		return switch (string.trim().toLowerCase()) {
			case "causal" -> AllocationMethod.CAUSAL;
			case "economic" -> AllocationMethod.ECONOMIC;
			case "physical" -> AllocationMethod.PHYSICAL;
			default -> AllocationMethod.NONE;
		};
	}

	private void readFactors(List<Exchange> products) {
		int row = 4;
		while (true) {
			AllocationFactor[] factors = readRowFactors(row, products);
			if (factors == null)
				break;
			process.allocationFactors.add(factors[0]);
			process.allocationFactors.add(factors[1]);
			row++;
		}
	}

	private AllocationFactor[] readRowFactors(int row,
			List<Exchange> products) {
		String name = wb.getString(sheet, row, 0);
		Exchange product = getProduct(name, products);
		if (product == null)
			return null;
		AllocationFactor[] factors = new AllocationFactor[2];
		factors[0] = new AllocationFactor();
		factors[0].productId = product.flow.id;
		factors[0].value = wb.getDouble(sheet, row, 2);
		factors[0].method = AllocationMethod.PHYSICAL;
		factors[1] = new AllocationFactor();
		factors[1].productId = product.flow.id;
		factors[1].value = wb.getDouble(sheet, row, 3);
		factors[1].method = AllocationMethod.ECONOMIC;
		return factors;
	}

	private void readCausalFactors(List<Exchange> products) {
		int causalStartRow = findCausalStartRow();
		if (causalStartRow == -1)
			return;
		HashMap<Integer, Long> map = getProductColumnMap(causalStartRow,
				products);
		int row = causalStartRow + 2;
		while (true) {
			Exchange exchange = getFactorExchange(row);
			if (exchange == null)
				break;
			createCausalFactors(row, exchange, map);
			row++;
		}
	}

	private void createCausalFactors(int row, Exchange exchange,
			HashMap<Integer, Long> productColumnMap) {
		for (Integer col : productColumnMap.keySet()) {
			Long productId = productColumnMap.get(col);
			if (col == null || productId == null)
				continue;
			AllocationFactor factor = new AllocationFactor();
			factor.method = AllocationMethod.CAUSAL;
			factor.productId = productId;
			factor.value = wb.getDouble(sheet, row, col);
			factor.exchange = exchange;
			process.allocationFactors.add(factor);
		}
	}

	private Exchange getFactorExchange(int row) {
		String name = wb.getString(sheet, row, 0);
		if (name == null)
			return null;
		String category = wb.getString(sheet, row, 1);
		Flow flow = wb.index.getFlow(name, category);
		String direction = wb.getString(sheet, row, 2);
		if (flow == null || direction == null)
			return null;
		boolean input = direction.equalsIgnoreCase("Input");
		for (Exchange exchange : process.exchanges) {
			if (exchange.isInput == input
					&& Objects.equals(exchange.flow, flow))
				return exchange;
		}
		return null;
	}

	private int findCausalStartRow() {
		int row = 5;
		while (row < 500) {
			String s = wb.getString(sheet, row, 0);
			if (s != null && s.equalsIgnoreCase("Causal allocation"))
				return row;
			row++;
		}
		return -1;
	}

	private HashMap<Integer, Long> getProductColumnMap(int causalStartRow,
			List<Exchange> products) {
		HashMap<Integer, Long> map = new HashMap<>();
		int row = causalStartRow + 1;
		int col = 4;
		while (col < 500) {
			String name = wb.getString(sheet, row, col);
			Exchange product = getProduct(name, products);
			if (product == null)
				break;
			map.put(col, product.flow.id);
			col++;
		}
		return map;
	}

	private List<Exchange> selectProducts() {
		List<Exchange> products = new ArrayList<>();
		for (Exchange exchange : wb.process.exchanges) {
			if (exchange.isInput || exchange.flow == null)
				continue;
			if (exchange.flow.flowType == FlowType.PRODUCT_FLOW)
				products.add(exchange);
		}
		return products;
	}

	private Exchange getProduct(String name, List<Exchange> products) {
		if (name == null)
			return null;
		for (Exchange exchange : products) {
			String flowName = exchange.flow.name;
			if (flowName == null)
				continue;
			if (name.equalsIgnoreCase(flowName))
				return exchange;
		}
		wb.log.warn("no output product found for name: " + name);
		return null;
	}
}
