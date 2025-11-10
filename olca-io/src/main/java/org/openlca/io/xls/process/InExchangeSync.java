package org.openlca.io.xls.process;

import java.util.function.Consumer;

import org.openlca.commons.Strings;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.util.Exchanges;

class InExchangeSync {

	private final InConfig config;
	private final Process process ;

	private InExchangeSync(InConfig config) {
		this.config = config;
		this.process = config.process();
		// TODO: we do not have a way currently to identify existing
		// exchanges. this can be a problem when linked processes
		// are updated
		process.exchanges.clear();
	}

	static void sync(InConfig config) {
		var sync = new InExchangeSync(config);
		sync.sync(Tab.INPUTS);
		sync.sync(Tab.OUTPUTS);
	}

	private void sync(Tab tab) {
		var sheet = config.getSheet(tab);
		if (sheet == null)
			return;
		sheet.eachRow(row -> {
			var e = new Exchange();
			e.isInput = tab == Tab.INPUTS;
			if (update(e, row)) {
				process.add(e);
				if (row.bool(Field.IS_REFERENCE)) {
					process.quantitativeReference = e;
				}
			}
		});
	}

	private boolean update(Exchange e, RowReader row) {

		// flow
		var name = row.str(Field.FLOW);
		if (name == null)
			return false;
		var category = row.str(Field.CATEGORY);
		e.flow = config.index().getFlow(name, category);
		if (e.flow == null) {
			config.log().error("unknown flow: " + category + "/" + name);
			return false;
		}

		// unit TODO: updates!
		setUnit(e, row);
		if (e.unit == null)
			return false;

		// amount
		var amount = row.value(Field.AMOUNT);
		if (amount instanceof Number num) {
			e.amount = num.doubleValue();
			e.formula = null;
		} else if (amount instanceof String s) {
			e.formula = s;
		} else {
			e.amount = 0;
			e.formula = null;
		}

		// costs
		e.currency = row.get(
			Field.CURRENCY, config, Currency.class);
		if (e.currency != null) {
			var costs = row.value(Field.COSTS_REVENUES);
			if (costs instanceof Number num) {
				e.costs = num.doubleValue();
				e.costFormula = null;
			} else if (costs instanceof String s) {
				e.costFormula = s;
				e.costs = null;
			}
		} else {
			e.costs = null;
			e.costFormula = null;
		}

		e.uncertainty = row.uncertainty();
		e.isAvoided = row.bool(Field.IS_AVOIDED);
		e.location = row.get(
			Field.LOCATION, config, Location.class);
		e.dqEntry = row.str(Field.DATA_QUALITY_ENTRY);
		e.description = row.str(Field.DESCRIPTION);

		// provider
		if (!Exchanges.isLinkable(e)) {
			e.defaultProviderId = 0;
		} else {
			var provider = row.str(Field.PROVIDER);
			var providerId = config.index().getProviderId(provider);
			if(Strings.isNotBlank(providerId)) {
				config.providers().add(providerId, e);
			}
		}

		return true;
	}

	private void setUnit(Exchange e, RowReader row) {
		Consumer<String> err = message -> {
			config.log().error(message + " in row " + row.rowNum());
			e.unit = null;
			e.flowPropertyFactor = null;
		};
		if (e.flow == null) {
			err.accept("no flow -> no units");
			return;
		}
		var unitName = row.str(Field.UNIT);
		if (Strings.isBlank(unitName)) {
			err.accept("no unit defined");
			return;
		}

		Unit unit = null;
		FlowPropertyFactor factor = null;
		for (var f : e.flow.flowPropertyFactors) {
			if (f.flowProperty == null
					|| f.flowProperty.unitGroup == null)
				continue;
			var group = f.flowProperty.unitGroup;
			var u = group.getUnit(unitName);
			if (u != null) {
				unit = u;
				factor = f;
				break;
			}
		}

		if (unit == null) {
			err.accept("unknown unit " + unitName
					+ " for flow " + EntityIndex.flowKeyOf(e.flow));
			return;
		}
		e.unit = unit;
		e.flowPropertyFactor = factor;
	}

}
