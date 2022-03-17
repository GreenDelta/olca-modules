package org.openlca.io.ilcd.input;

import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.FlowBag;
import org.openlca.ilcd.util.Flows;
import org.openlca.io.maps.SyncFlow;
import org.openlca.util.Strings;

public class FlowImport {

	private final ImportConfig config;
	private FlowBag ilcdFlow;
	private Flow flow;

	public FlowImport(ImportConfig config) {
		this.config = config;
	}

	public SyncFlow run(org.openlca.ilcd.flows.Flow dataSet) {
		return config.flowSync().createIfAbsent(
			dataSet.getUUID(), () -> createNew(dataSet));
	}

	public static SyncFlow get(ImportConfig config, String id) {
		return config.flowSync().createIfAbsent(id, () -> {
			var dataSet = config.store().get(org.openlca.ilcd.flows.Flow.class, id);
			if (dataSet == null) {
				config.log().error("invalid reference in ILCD data set:" +
					" flow '" + id + "' does not exist");
				return null;
			}
			return new FlowImport(config).createNew(dataSet);
		});
	}

	private Flow createNew(org.openlca.ilcd.flows.Flow dataSet) {
		this.ilcdFlow = new FlowBag(dataSet, config.langOrder());
		flow = new Flow();
		String[] path = Categories.getPath(ilcdFlow.flow);
		flow.category = new CategoryDao(config.db())
			.sync(ModelType.FLOW, path);
		createAndMapContent();
		if (flow.referenceFlowProperty == null) {
			config.log().error("Could not import flow "
				+ flow.refId + " because the "
				+ "reference flow property of this flow "
				+ "could not be imported.");
			return null;
		}
		return config.insert(flow);
	}

	private void createAndMapContent() {
		flow.refId = ilcdFlow.getId();
		flow.name = Strings.cut(
			Flows.getFullName(ilcdFlow.flow, ilcdFlow.langs), 2048);
		flow.flowType = flowType();
		flow.description = ilcdFlow.getComment();
		flow.casNumber = ilcdFlow.getCasNumber();
		flow.synonyms = ilcdFlow.getSynonyms();
		flow.formula = ilcdFlow.getSumFormula();
		flow.version = Version.fromString(
			ilcdFlow.flow.version).getValue();
		var time = ilcdFlow.getTimeStamp();
		flow.lastChange = time != null
			? time.getTime()
			: System.currentTimeMillis();
		var locationCode = config.str(ilcdFlow.getLocation());
		flow.location = config.locationOf(locationCode);
		addFlowProperties();
	}

	private void addFlowProperties() {
		Integer refID = Flows.getReferenceFlowPropertyID(ilcdFlow.flow);
		List<FlowPropertyRef> refs = Flows
			.getFlowProperties(ilcdFlow.flow);
		for (FlowPropertyRef ref : refs) {
			if (ref == null || ref.flowProperty == null)
				continue;
			var property = FlowPropertyImport.get(config, ref.flowProperty.uuid);
			if (property == null)
				continue;
			var factor = new FlowPropertyFactor();
			factor.flowProperty = property;
			factor.conversionFactor = ref.meanValue;
			flow.flowPropertyFactors.add(factor);
			Integer propID = ref.dataSetInternalID;
			if (refID == null || propID == null)
				continue;
			if (refID.intValue() == propID.intValue())
				flow.referenceFlowProperty = property;
		}
	}

	private FlowType flowType() {
		var type = Flows.getType(ilcdFlow.flow);
		if (type == null)
			return FlowType.ELEMENTARY_FLOW;
		return switch (type) {
			case ELEMENTARY_FLOW -> FlowType.ELEMENTARY_FLOW;
			case PRODUCT_FLOW, OTHER_FLOW -> FlowType.PRODUCT_FLOW;
			case WASTE_FLOW -> FlowType.WASTE_FLOW;
		};
	}
}
