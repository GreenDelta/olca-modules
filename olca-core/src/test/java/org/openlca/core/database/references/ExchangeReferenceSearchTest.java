package org.openlca.core.database.references;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.Tests;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class ExchangeReferenceSearchTest extends BaseReferenceSearchTest {

	private Map<Long, Class<? extends AbstractEntity>> ownerTypes = new HashMap<>();
	private Map<Long, Long> ownerIds = new HashMap<>();

	@Override
	protected ModelType getModelType() {
		return null;
	}

	@Override
	protected Class<? extends AbstractEntity> getModelClass() {
		return Exchange.class;
	}

	@Override
	protected boolean isNestedSearchTest() {
		return true;
	}

	@Override
	protected List<Reference> findReferences(Set<Long> ids) {
		return new ExchangeReferenceSearch(Tests.getDb(), ownerTypes, ownerIds)
				.findReferences(ids);
	}

	@Override
	protected Exchange createModel() {
		Process process = new Process();
		Flow flow = new Flow();
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(Tests.insert(new FlowProperty()));
		flow.getFlowPropertyFactors().add(factor);
		flow = Tests.insert(flow);
		UnitGroup group = new UnitGroup();
		Unit unit = new Unit();
		unit.setName("unit");
		group.getUnits().add(unit);
		group = Tests.insert(group);
		factor = flow.getFactor(factor.getFlowProperty());
		unit = group.getUnit(unit.getName());
		Exchange exchange = new Exchange();
		final Flow flow1 = flow;
		exchange.flow = flow1;
		exchange.flowPropertyFactor = factor;
		final Unit unit1 = unit;
		exchange.unit = unit1;
		process.getExchanges().add(exchange);
		process = Tests.insert(process);
		exchange = process.getExchanges().get(0);
		ownerIds.put(exchange.getId(), process.getId());
		ownerTypes.put(exchange.getId(), Process.class);
		addExpected(new Reference("flow", Flow.class, flow.getId(),
				Process.class, process.getId(), "exchanges", Exchange.class,
				exchange.getId(), false));
		addExpected(new Reference("flowPropertyFactor",
				FlowPropertyFactor.class, factor.getId(), Process.class,
				process.getId(), "exchanges", Exchange.class, exchange.getId(),
				false));
		addExpected(new Reference("unit", Unit.class, unit.getId(),
				Process.class, process.getId(), "exchanges", Exchange.class,
				exchange.getId(), false));
		return exchange;
	}

}
