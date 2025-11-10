package org.openlca.io.xls.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.util.Exchanges;

class InAllocationSync {

	private final InConfig config;
	private final Process process;

	private InAllocationSync(InConfig wb) {
		this.config = wb;
		this.process = wb.process();
		process.allocationFactors.clear();
	}

	static void sync(InConfig config) {
		new InAllocationSync(config).sync();
	}

	private void sync() {
		var sheet = config.getSheet(Tab.ALLOCATION);
		if (sheet == null)
			return;
		process.defaultAllocationMethod = getMethod(
			In.stringOf(sheet.sheetObject(), 0, 1));
		var providerFlows = config.process().exchanges.stream()
			.filter(Exchanges::isProviderFlow)
			.toList();
		if (providerFlows.size() <= 1)
			return;
		readSimpleFactors(sheet, providerFlows);
		readCausalFactors(sheet, providerFlows);
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

	private void readSimpleFactors(SheetReader sheet, List<Exchange> providers) {
		sheet.eachRow(Section.PHYSICAL_ECONOMIC_ALLOCATION, row -> {
			var name = row.str(Field.PRODUCT);
			var provider = find(name, providers);
			if (provider == null)
				return;
			process.allocationFactors.add(AllocationFactor.physical(
				provider, row.num(Field.PHYSICAL)));
			process.allocationFactors.add(AllocationFactor.economic(
					provider, row.num(Field.ECONOMIC)));
		});
	}

	private void readCausalFactors(SheetReader sheet, List<Exchange> providers) {
		var providerOffset = 4;
		var providerColumns = new ArrayList<String>();
		var fieldsRef = new AtomicReference<FieldMap>();
		sheet.eachRowObject(Section.CAUSAL_ALLOCATION, rowObject -> {
			var fields = fieldsRef.get();
			if (fields == null) {
				// parse first row
				fields = FieldMap.of(rowObject);
				fieldsRef.set(fields);
				int col = providerOffset;
				String providerName;
				while((providerName = In.stringOf(rowObject, col)) != null) {
					providerColumns.add(providerName);
					col++;
				}
			} else {
				var row = RowReader.of(rowObject, fields);
				var exchange = getCausalAllocatedExchange(row);
				if (exchange == null)
					return;
				for (int i = 0; i < providerColumns.size(); i++) {
					var providerName = providerColumns.get(i);
					var provider = find(providerName, providers);
					if (provider == null)
						continue;
					double v = In.doubleOf(In.cell(rowObject, providerOffset + i));
					var factor = AllocationFactor.causal(provider, exchange, v);
					process.allocationFactors.add(factor);
				}
			}
		});
	}

	private Exchange getCausalAllocatedExchange(RowReader row) {
		var name = row.str(Field.FLOW);
		if (name == null)
			return null;
		var category = row.str(Field.CATEGORY);
		var flow = config.index().getFlow(name, category);
		var direction = row.str(Field.DIRECTION);
		if (flow == null || direction == null)
			return null;
		boolean input = direction.equalsIgnoreCase("Input");
		for (var exchange : process.exchanges) {
			if (exchange.isInput == input
				&& Objects.equals(exchange.flow, flow))
				return exchange;
		}
		config.log().warn(
			"allocation: allocated exchange not found: "
				+ category + "/" + name);
		return null;
	}

	private Flow find(String name, List<Exchange> exchanges) {
		if (name == null)
			return null;
		for (var e : exchanges) {
			if (e.flow == null)
				continue;
			if (name.equalsIgnoreCase(e.flow.name))
				return e.flow;
		}
		config.log().warn("allocation: no provider flow found for name: " + name);
		return null;
	}
}
