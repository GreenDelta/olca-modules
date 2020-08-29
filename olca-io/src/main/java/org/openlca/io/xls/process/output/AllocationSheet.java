package org.openlca.io.xls.process.output;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

class AllocationSheet {

	private final Config config;
	private final Sheet sheet;
	private int row = 0;

	private AllocationSheet(Config config) {
		this.config = config;
		sheet = config.workbook.createSheet("Allocation");
	}

	public static void write(Config config) {
		new AllocationSheet(config).write();
	}

	private void write() {
		Excel.trackSize(sheet, 0, 3);
		config.pair(sheet, row++, "Default allocation method",
				getAllocationMethod());
		List<Exchange> outputs = getProducts();
		writeFactorSection(outputs);
		row += 2;
		writeCausalFactorSection(outputs);
		Excel.autoSize(sheet, 0, 3);
	}

	private String getAllocationMethod() {
		AllocationMethod method = config.process.defaultAllocationMethod;
		if (method == null)
			return "none";
		switch (method) {
			case CAUSAL:
				return "causal";
			case ECONOMIC:
				return "economic";
			case PHYSICAL:
				return "physical";
			default:
				return "none";
		}
	}

	private void writeFactorSection(List<Exchange> outputs) {
		row++;
		config.header(sheet, row++, 0, "Physical & economic allocation");
		config.header(sheet, row, 0, "Product");
		config.header(sheet, row, 1, "Category");
		config.header(sheet, row, 2, "Physical");
		config.header(sheet, row, 3, "Economic");
		for (Exchange product : outputs) {
			row++;
			writeFactors(product);
		}
	}

	private void writeFactors(Exchange product) {
		Excel.cell(sheet, row, 0, product.flow.name);
		Excel.cell(sheet, row, 1, CategoryPath.getFull(
				product.flow.category));
		Excel.cell(sheet, row, 2, getFactor(product,
				AllocationMethod.PHYSICAL));
		Excel.cell(sheet, row, 3, getFactor(product,
				AllocationMethod.ECONOMIC));
	}

	private void writeCausalFactorSection(List<Exchange> outputs) {
		row++;
		config.header(sheet, row++, 0, "Causal allocation");
		config.header(sheet, row, 0, "Flow");
		config.header(sheet, row, 1, "Category");
		config.header(sheet, row, 2, "Direction");
		config.header(sheet, row, 3, "Amount");
		for (int i = 0; i < outputs.size(); i++)
			config.header(sheet, row, 4 + i, outputs.get(i).flow.name);
		for (Exchange flow : getNonProducts()) {
			row++;
			writeCausalRowInfo(flow);
			for (int i = 0; i < outputs.size(); i++) {
				Exchange product = outputs.get(i);
				Excel.cell(sheet, row, 4 + i, getCausalFactor(product, flow));
			}
		}
	}

	private void writeCausalRowInfo(Exchange e) {
		if(e.flow == null)
			return;
		Excel.cell(sheet, row, 0, e.flow.name);
		Excel.cell(sheet, row, 1, CategoryPath.getFull(e.flow.category));
		String direction = e.isInput ? "Input" : "Output";
		Excel.cell(sheet, row, 2, direction);
		String amount = Double.toString(e.amount);
		if(e.unit != null)
			amount += " " + e.unit.name;
		Excel.cell(sheet, row, 3, amount);
	}

	private List<Exchange> getProducts() {
		List<Exchange> outputs = new ArrayList<>();
		for (Exchange exchange : config.process.exchanges) {
			if (isOutputProduct(exchange))
				outputs.add(exchange);
		}
		outputs.sort(new ExchangeSorter());
		return outputs;
	}

	private List<Exchange> getNonProducts() {
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : config.process.exchanges) {
			if (!isOutputProduct(exchange))
				exchanges.add(exchange);
		}
		exchanges.sort(new ExchangeSorter());
		return exchanges;
	}

	private boolean isOutputProduct(Exchange exchange) {
		return exchange != null
				&& exchange.flow != null
				&& !exchange.isInput
				&& !exchange.isAvoided
				&& exchange.flow.flowType == FlowType.PRODUCT_FLOW;
	}

	private double getFactor(Exchange product, AllocationMethod method) {
		for (AllocationFactor factor : config.process.allocationFactors) {
			if (method == factor.method
					&& factor.productId == product.flow.id)
				return factor.value;
		}
		return 1.0;
	}

	private double getCausalFactor(Exchange product, Exchange flow) {
		for (AllocationFactor factor : config.process.allocationFactors) {
			if (factor.method != AllocationMethod.CAUSAL)
				continue;
			if (factor.productId == product.flow.id
					&& Objects.equals(factor.exchange, flow))
				return factor.value;
		}
		return 1.0;
	}

	private static class ExchangeSorter implements Comparator<Exchange> {
		@Override
		public int compare(Exchange e1, Exchange e2) {
			return Strings.compare(
					e1.flow.name,
					e2.flow.name);
		}
	}
}
