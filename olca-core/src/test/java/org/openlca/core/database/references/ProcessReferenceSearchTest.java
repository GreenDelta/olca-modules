package org.openlca.core.database.references;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.Tests;
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

	private Map<Long, Process> processes = new HashMap<>();

	@Override
	protected ModelType getModelType() {
		return ModelType.PROCESS;
	}

	@Override
	protected Process createModel() {
		Process process = new Process();
		process.setCategory(insertAndAddExpected("category", new Category()));
		process.setLocation(insertAndAddExpected("location", new Location()));
		String n1 = generateName();
		String n2 = generateName();
		String n3 = generateName();
		String n4 = generateName();
		String n5 = generateName();
		process.getExchanges().add(createExchange(3d, true));
		process.getExchanges().add(createExchange("2*" + n4, false));
		process.getParameters().add(createParameter(n1, 3d, false));
		process.getParameters()
				.add(createParameter(n2, n1 + "*2*" + n3, false));
		process.socialAspects.add(createSocialAspect());
		process.socialAspects.add(createSocialAspect());
		process.currency = insertAndAddExpected("currency", new Currency());
		process.setDocumentation(createDocumentation());
		insertAndAddExpected(null, createParameter(n3, "5*5", true));
		// formula with parameter to see if added as reference (unexpected)
		insertAndAddExpected(null, createParameter(n4, "3*" + n5, true));
		Parameter globalUnreferenced = createParameter(n1, "3*3", true);
		Parameter globalUnreferenced2 = createParameter(n5, "3*3", true);
		// must be inserted manually
		globalUnreferenced = Tests.insert(globalUnreferenced);
		globalUnreferenced2 = Tests.insert(globalUnreferenced2);
		process = Tests.insert(process);
		for (Exchange e : process.getExchanges()) {
			addExpected("flow", e.flow, "exchanges", Exchange.class,
					e.getId());
			addExpected("flowPropertyFactor", e.flowPropertyFactor,
					"exchanges", Exchange.class, e.getId());
			addExpected("unit", e.unit, "exchanges", Exchange.class,
					e.getId());
			Process provider = processes.get(e.defaultProviderId);
			if (provider != null)
				addExpected("defaultProviderId", provider, "exchanges",
						Exchange.class, e.getId());
		}
		for (SocialAspect a : process.socialAspects) {
			addExpected("indicator", a.indicator, "socialAspects",
					SocialAspect.class, a.getId());
			addExpected("source", a.source, "socialAspects",
					SocialAspect.class, a.getId());
		}
		ProcessDocumentation doc = process.getDocumentation();
		addExpected("dataDocumentor", doc.getDataDocumentor(), "documentation",
				ProcessDocumentation.class, doc.getId());
		addExpected("dataGenerator", doc.getDataGenerator(), "documentation",
				ProcessDocumentation.class, doc.getId());
		addExpected("dataSetOwner", doc.getDataSetOwner(), "documentation",
				ProcessDocumentation.class, doc.getId());
		addExpected("reviewer", doc.getReviewer(), "documentation",
				ProcessDocumentation.class, doc.getId());
		addExpected("publication", doc.getPublication(), "documentation",
				ProcessDocumentation.class, doc.getId());
		for (Source s : process.getDocumentation().getSources())
			addExpected("sources", s, "documentation",
					ProcessDocumentation.class, doc.getId());
		return process;
	}

	private Exchange createExchange(Object value, boolean provider) {
		Exchange exchange = new Exchange();
		exchange.flow = createFlow();
		exchange.flowPropertyFactor = exchange.flow
		.getFlowPropertyFactors().get(0);
		exchange.unit = exchange.flowPropertyFactor.getFlowProperty()
		.getUnitGroup().getUnits().get(0);
		boolean formula = value instanceof String;
		if (formula)
			exchange.amountFormula = value.toString();
		else
			exchange.amount = (double) value;
		if (provider) {
			Process process = Tests.insert(new Process());
			processes.put(process.getId(), process);
			exchange.defaultProviderId = process.getId();
		}
		return exchange;
	}

	private Flow createFlow() {
		Flow flow = new Flow();
		UnitGroup group = new UnitGroup();
		Unit unit = new Unit();
		unit.setName("unit");
		group.getUnits().add(unit);
		group = Tests.insert(group);
		FlowProperty property = new FlowProperty();
		property.setUnitGroup(group);
		property = Tests.insert(property);
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(property);
		flow.getFlowPropertyFactors().add(factor);
		return Tests.insert(flow);
	}

	private SocialAspect createSocialAspect() {
		SocialAspect aspect = new SocialAspect();
		aspect.indicator = Tests.insert(new SocialIndicator());
		aspect.source = Tests.insert(new Source());
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
		doc.setDataDocumentor(Tests.insert(new Actor()));
		doc.setDataGenerator(Tests.insert(new Actor()));
		doc.setDataSetOwner(Tests.insert(new Actor()));
		doc.setReviewer(Tests.insert(new Actor()));
		doc.setPublication(Tests.insert(new Source()));
		doc.getSources().add(Tests.insert(new Source()));
		doc.getSources().add(Tests.insert(new Source()));
		return doc;
	}

}
