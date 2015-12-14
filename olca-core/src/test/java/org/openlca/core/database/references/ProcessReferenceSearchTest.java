package org.openlca.core.database.references;

import org.junit.After;
import org.openlca.core.Tests;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class ProcessReferenceSearchTest extends BaseReferenceSearchTest {

	@After
	public void deleteParameter() {
		new ParameterDao(Tests.getDb()).deleteAll();
	}

	@Override
	protected ModelType getModelType() {
		return ModelType.PROCESS;
	}

	@Override
	protected Process createModel() {
		Process process = new Process();
		process.setCategory(addExpected(new Category()));
		process.setLocation(addExpected(new Location()));
		process.getExchanges().add(createExchange(3d, true));
		process.getExchanges().add(createExchange("2*p4", false));
		process.getParameters().add(createParameter("p1", 3d, false));
		process.getParameters().add(createParameter("p2", "p1*2*p3", false));
		process.socialAspects.add(createSocialAspect());
		process.socialAspects.add(createSocialAspect());
		process.currency = addExpected(new Currency());
		process.setDocumentation(createDocumentation());
		addExpected(createParameter("p3", "5*5", true));
		// formula with parameter to see if added as reference (unexpected)
		addExpected(createParameter("p4", "3*p5", true));
		Parameter globalUnreferenced = createParameter("p1", "3*3", true);
		Parameter globalUnreferenced2 = createParameter("p5", "3*3", true);
		// must be inserted manually
		globalUnreferenced = Tests.insert(globalUnreferenced);
		globalUnreferenced2 = Tests.insert(globalUnreferenced2);
		return process;
	}

	private Exchange createExchange(Object value, boolean provider) {
		Exchange exchange = new Exchange();
		exchange.setFlow(createFlow());
		exchange.setFlowPropertyFactor(exchange.getFlow()
				.getFlowPropertyFactors().get(0));
		exchange.setUnit(exchange.getFlowPropertyFactor().getFlowProperty()
				.getUnitGroup().getUnits().get(0));
		boolean formula = value instanceof String;
		if (formula)
			exchange.setAmountFormula(value.toString());
		else
			exchange.setAmountValue((double) value);
		if (provider)
			exchange.setDefaultProviderId(addExpected(new Process()).getId());
		return exchange;
	}

	private Flow createFlow() {
		Flow flow = new Flow();
		FlowProperty property = new FlowProperty();
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(property);
		UnitGroup group = new UnitGroup();
		Unit unit = new Unit();
		group.getUnits().add(unit);
		property.setUnitGroup(group);
		flow.getFlowPropertyFactors().add(factor);
		addExpected(group);
		addExpected(property);
		return addExpected(flow);
	}

	private SocialAspect createSocialAspect() {
		SocialAspect aspect = new SocialAspect();
		aspect.indicator = addExpected(new SocialIndicator());
		aspect.source = addExpected(new Source());
		return aspect;
	}

	private Parameter createParameter(String name, Object value, boolean global) {
		Parameter parameter = new Parameter();
		parameter.setName(name);
		boolean formula = value instanceof String;
		parameter.setInputParameter(!formula);
		if (formula)
			parameter.setFormula(value.toString());
		else
			parameter.setValue((double) value);
		if (global)
			parameter.setScope(ParameterScope.GLOBAL);
		else
			parameter.setScope(ParameterScope.PROCESS);
		return parameter;
	}

	private ProcessDocumentation createDocumentation() {
		ProcessDocumentation doc = new ProcessDocumentation();
		doc.setDataDocumentor(addExpected(new Actor()));
		doc.setDataGenerator(addExpected(new Actor()));
		doc.setDataSetOwner(addExpected(new Actor()));
		doc.setReviewer(addExpected(new Actor()));
		doc.setPublication(addExpected(new Source()));
		doc.getSources().add(addExpected(new Source()));
		doc.getSources().add(addExpected(new Source()));
		return doc;
	}

}
