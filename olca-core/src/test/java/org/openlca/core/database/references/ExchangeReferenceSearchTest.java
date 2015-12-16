package org.openlca.core.database.references;

import java.util.Collections;
import java.util.List;

import org.openlca.core.Tests;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class ExchangeReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return null;
	}

	@Override
	protected Class<?> getModelClass() {
		return Exchange.class;
	}

	@Override
	protected List<Reference> findReferences(long id) {
		return new ExchangeReferenceSearch(Tests.getDb())
				.findReferences(Collections.singleton(id));
	}

	@Override
	protected Exchange createModel() {
		Process process = new Process();
		Flow flow = new Flow();
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(Tests.insert(new FlowProperty()));
		flow.getFlowPropertyFactors().add(factor);
		flow = insertAndAddExpected(flow);
		UnitGroup group = new UnitGroup();
		Unit unit = new Unit();
		unit.setName("unit");
		group.getUnits().add(unit);
		group = Tests.insert(group);
		factor = flow.getFactor(factor.getFlowProperty());
		unit = group.getUnit(unit.getName());
		addExpected(factor);
		addExpected(unit);
		Exchange exchange = new Exchange();
		exchange.setFlow(flow);
		exchange.setFlowPropertyFactor(factor);
		exchange.setUnit(unit);
		process.getExchanges().add(exchange);
		process = Tests.insert(process);
		return process.getExchanges().get(0);
	}

}
