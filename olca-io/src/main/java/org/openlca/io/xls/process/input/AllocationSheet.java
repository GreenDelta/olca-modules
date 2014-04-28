package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

class AllocationSheet {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final Config config;
	private final Process process;
	private final Sheet sheet;

	private AllocationSheet(Config config) {
		this.config = config;
		this.process = config.process;
		this.sheet = config.workbook.getSheet("Allocation");
	}

	public static void read(Config config) {
		new AllocationSheet(config).read();
	}

	private void read() {
		if (sheet == null || process == null)
			return;
		try {
			List<Exchange> products = selectProducts();
			if (products.size() <= 1) {
				log.trace("process is not a multi-output process "
						+ "-> no allocation factors imported");
				return;
			}
			log.trace("read allocation factors");
			readDefaultMethod();
			readFactors(products);
			readCausalFactors(products);
		} catch (Exception e) {
			log.error("failed to read allocation factors", e);
		}
	}

	private void readDefaultMethod() {
		String methodString = config.getString(sheet, 1, 1);
		AllocationMethod method = getMethod(methodString);
		process.setDefaultAllocationMethod(method);
	}

	private AllocationMethod getMethod(String string) {
		if (string == null)
			return AllocationMethod.NONE;
		switch (string.trim().toLowerCase()) {
		case "causal":
			return AllocationMethod.CAUSAL;
		case "economic":
			return AllocationMethod.ECONOMIC;
		case "physical":
			return AllocationMethod.PHYSICAL;
		default:
			return AllocationMethod.NONE;
		}
	}

	private void readFactors(List<Exchange> products) {
		int row = 4;
		while (true) {
			AllocationFactor[] factors = readRowFactors(row, products);
			if (factors == null)
				break;
			process.getAllocationFactors().add(factors[0]);
			process.getAllocationFactors().add(factors[1]);
			row++;
		}
	}

	private AllocationFactor[] readRowFactors(int row, List<Exchange> products) {
		String name = config.getString(sheet, row, 0);
		Exchange product = getProduct(name, products);
		if (product == null)
			return null;
		AllocationFactor[] factors = new AllocationFactor[2];
		factors[0] = new AllocationFactor();
		factors[0].setProductId(product.getFlow().getId());
		factors[0].setValue(config.getDouble(sheet, row, 2));
		factors[0].setAllocationType(AllocationMethod.PHYSICAL);
		factors[1] = new AllocationFactor();
		factors[1].setProductId(product.getFlow().getId());
		factors[1].setValue(config.getDouble(sheet, row, 3));
		factors[1].setAllocationType(AllocationMethod.ECONOMIC);
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
			factor.setAllocationType(AllocationMethod.CAUSAL);
			factor.setProductId(productId);
			factor.setValue(config.getDouble(sheet, row, col));
			factor.setExchange(exchange);
			process.getAllocationFactors().add(factor);
		}
	}

	private Exchange getFactorExchange(int row) {
		String name = config.getString(sheet, row, 0);
		if (name == null)
			return null;
		String category = config.getString(sheet, row, 1);
		Flow flow = config.refData.getFlow(name, category);
		String direction = config.getString(sheet, row, 2);
		if (flow == null || direction == null)
			return null;
		boolean input = direction.equalsIgnoreCase("Input");
		for (Exchange exchange : process.getExchanges()) {
			if (exchange.isInput() == input
					&& Objects.equals(exchange.getFlow(), flow))
				return exchange;
		}
		return null;
	}

	private int findCausalStartRow() {
		int row = 5;
		while (row < 500) {
			String s = config.getString(sheet, row, 0);
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
			String name = config.getString(sheet, row, col);
			Exchange product = getProduct(name, products);
			if (product == null)
				break;
			map.put(col, product.getFlow().getId());
			col++;
		}
		return map;
	}

	private List<Exchange> selectProducts() {
		List<Exchange> products = new ArrayList<>();
		for (Exchange exchange : config.process.getExchanges()) {
			if (exchange.isInput() || exchange.getFlow() == null)
				continue;
			if (exchange.getFlow().getFlowType() == FlowType.PRODUCT_FLOW)
				products.add(exchange);
		}
		return products;
	}

	private Exchange getProduct(String name, List<Exchange> products) {
		if (name == null)
			return null;
		for (Exchange exchange : products) {
			String flowName = exchange.getFlow().getName();
			if (flowName == null)
				continue;
			if (name.equalsIgnoreCase(flowName))
				return exchange;
		}
		log.warn("no output product found for name {}", name);
		return null;
	}
}
