package org.openlca.io.xls.process;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.openlca.commons.Strings;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.util.Exchanges;

class OutAllocationSync {

	private final OutConfig config;
	private final Process process;

	private OutAllocationSync(OutConfig config) {
		this.config = config;
		this.process = config.process();
	}

	static void sync(OutConfig config) {
		new OutAllocationSync(config).write();
	}

	private void write() {
		var sheet = config.createSheet(Tab.ALLOCATION)
			.withColumnWidths(4, 25)
			.next(Field.DEFAULT_ALLOCATION_METHOD, defaultMethod());
		List<Exchange> outputs = getProviderFlows();
		writeSimpleFactors(sheet, outputs);
		sheet.next().next();
		writeCausalFactors(sheet, outputs);
	}

	private String defaultMethod() {
		var method = process.defaultAllocationMethod;
		if (method == null)
			return "none";
		return switch (method) {
			case CAUSAL -> "causal";
			case ECONOMIC -> "economic";
			case PHYSICAL -> "physical";
			default -> "none";
		};
	}

	private void writeSimpleFactors(
		SheetWriter sheet, List<Exchange> providerFlows) {
		sheet.next(Section.PHYSICAL_ECONOMIC_ALLOCATION);
		sheet.header(
			Field.PRODUCT,
			Field.CATEGORY,
			Field.PHYSICAL,
			Field.ECONOMIC);
		if (providerFlows.size() < 2)
			return;
		for (var e : providerFlows) {
			sheet.next(row ->
				row.next(e.flow.name)
					.next(Out.pathOf(e.flow))
					.next(getFactor(e, AllocationMethod.PHYSICAL))
					.next(getFactor(e, AllocationMethod.ECONOMIC)));
		}
	}


	private void writeCausalFactors(
		SheetWriter sheet, List<Exchange> providerFlows) {

		sheet.next(Section.CAUSAL_ALLOCATION);
		sheet.header(
			Field.FLOW,
			Field.CATEGORY,
			Field.DIRECTION,
			Field.AMOUNT);
		var rowObj = sheet.row(sheet.rowCursor() - 1);
		for (int i = 0; i < providerFlows.size(); i++) {
			sheet.cell(rowObj, 4 + i, providerFlows.get(i).flow.name);
		}
		if (providerFlows.size() < 2)
			return;

		for (var e : getAllocatableFlows()) {
			sheet.next(row -> {
				row.next(e.flow.name)
					.next(Out.pathOf(e.flow))
					.next(e.isInput ? "Input" : "Output");
				var amount = Double.toString(e.amount);
				if (e.unit != null) {
					amount += " " + e.unit.name;
				}
				row.next(amount);
				for (var product : providerFlows) {
					row.next(getCausalFactor(product, e));
				}
			});
		}
	}

	private List<Exchange> getProviderFlows() {
		return process.exchanges.stream()
			.filter(Exchanges::isProviderFlow)
			.sorted(new ExchangeSorter())
			.toList();
	}

	private List<Exchange> getAllocatableFlows() {
		return process.exchanges.stream()
			.filter(e -> e.flow != null
				&& !Exchanges.isProviderFlow(e)
				&& !e.isAvoided)
			.sorted(new ExchangeSorter())
			.toList();
	}

	private double getFactor(Exchange product, AllocationMethod method) {
		for (var factor : process.allocationFactors) {
			if (method == factor.method
				&& factor.productId == product.flow.id)
				return factor.value;
		}
		return 1.0;
	}

	private double getCausalFactor(Exchange providerFlow, Exchange e) {
		for (var factor : process.allocationFactors) {
			if (factor.method != AllocationMethod.CAUSAL)
				continue;
			if (factor.productId == providerFlow.flow.id
				&& Objects.equals(factor.exchange, e))
				return factor.value;
		}
		return 1.0;
	}

	private static class ExchangeSorter implements Comparator<Exchange> {
		@Override
		public int compare(Exchange e1, Exchange e2) {
			return Strings.compareIgnoreCase(e1.flow.name, e2.flow.name);
		}
	}
}
