package org.openlca.io.xls.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.poi.ss.usermodel.Row;
import org.openlca.commons.Strings;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.util.Exchanges;

class OutExchangeSync {

	private final OutConfig config;

	private OutExchangeSync(OutConfig config) {
		this.config = config;
	}

	static void sync(OutConfig config) {
		var sync = new OutExchangeSync(config);
		sync.write(Tab.INPUTS);
		sync.write(Tab.OUTPUTS);
	}

	private void write(Tab tab) {
		var exchanges = getExchanges(tab);
		var sheet = config.createSheet(tab);
		writeHeader(sheet);
		for (var e : exchanges) {
			if (e.flow == null)
				continue;
			// visit dependencies
			config.visit(e.flow);
			config.visit(e.currency);
			config.visit(e.location);
			write(e, sheet);
		}
	}

	private void writeHeader(SheetWriter sheet) {
		sheet
			.withColumnWidths(17, 25)
			.header(
				Field.IS_REFERENCE,
				Field.FLOW,
				Field.CATEGORY,
				Field.AMOUNT,
				Field.UNIT,
				Field.COSTS_REVENUES,
				Field.CURRENCY,
				Field.UNCERTAINTY,
				Field.MEAN_MODE,
				Field.SD,
				Field.MINIMUM,
				Field.MAXIMUM,
				Field.IS_AVOIDED,
				Field.PROVIDER,
				Field.DATA_QUALITY_ENTRY,
				Field.LOCATION,
				Field.DESCRIPTION);
	}

	private void write(Exchange e, SheetWriter sheet) {
		boolean isRef = Objects.equals(e, config.process().quantitativeReference);
		var rowRef = new AtomicReference<Row>();
		sheet.next(row -> {
			rowRef.set(row.rowObject());

			// flow, amount, unit
			row.next(isRef ? "x" : null)
				.next(e.flow.name)
				.next(Out.pathOf(e.flow));
			if (Strings.isNotBlank(e.formula)) {
				row.next(e.formula);
			} else {
				row.next(e.amount);
			}
			row.next(e.unit != null ? e.unit.name : null);

			// costs
			if (e.currency != null) {
				if (Strings.isNotBlank(e.costFormula)) {
					row.next(e.costFormula);
				} else if (e.costs != null) {
					row.next(e.costs);
				}
				row.next(e.currency.name);
			} else {
				row.next().next();
			}

			row.next(e.uncertainty)
				.next(e.isAvoided ? "x" : null);

			// provider
			Process provider = null;
			if (e.defaultProviderId > 0 && Exchanges.isLinkable(e)) {
				provider = config.db().get(Process.class, e.defaultProviderId);
			}
			if (provider != null) {
				config.visit(provider);
				row.next(provider.name);
			} else {
				row.next();
			}

			row.next(e.dqEntry)
				.next(e.location != null
					? e.location.name
					: null)
				.next(e.description);
		});

		if (isRef && rowRef.get() != null) {
			var row = rowRef.get();
			row.cellIterator()
				.forEachRemaining(cell ->
					cell.setCellStyle(config.styles().bold()));
		}
	}

	private List<Exchange> getExchanges(Tab tab) {
		var forInputs = tab == Tab.INPUTS;
		var exchanges = new ArrayList<Exchange>();
		for (var e : config.process().exchanges) {
			if (e.isInput == forInputs)
				exchanges.add(e);
		}
		exchanges.sort((e1, e2) -> {
			if (e1.flow == null || e2.flow == null)
				return 0;
			return Strings.compareIgnoreCase(e1.flow.name, e2.flow.name);
		});
		return exchanges;
	}
}
