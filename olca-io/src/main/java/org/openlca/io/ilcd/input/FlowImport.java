package org.openlca.io.ilcd.input;

import java.util.Date;
import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.FlowBag;
import org.openlca.ilcd.util.Flows;
import org.openlca.util.Strings;

// TODO: the flow mapping of the config is currently not
// applied in the flow import!!
public class FlowImport {

	private final ImportConfig config;
	private FlowBag ilcdFlow;
	private Flow flow;

	public FlowImport(ImportConfig config) {
		this.config = config;
	}

	public Flow run(org.openlca.ilcd.flows.Flow dataSet) {
		this.ilcdFlow = new FlowBag(dataSet, config.langOrder());
		var flow = config.db().get(Flow.class, dataSet.getUUID());
		return flow != null
			? flow
			: createNew();
	}

	static Flow get(ImportConfig config, String id) {
		var flow = config.db().get(Flow.class, id);
		if (flow != null)
			return flow;
		var dataSet = config.store().get(
			org.openlca.ilcd.flows.Flow.class, id);
		if (dataSet == null) {
			config.log().error("invalid reference in ILCD data set:" +
				" flow '" + id + "' does not exist");
			return null;
		}
		return new FlowImport(config).run(dataSet);
	}

	private Flow createNew() {
		flow = new Flow();
		String[] cpath = Categories.getPath(ilcdFlow.flow);
		flow.category = new CategoryDao(config.db())
			.sync(ModelType.FLOW, cpath);
		createAndMapContent();
		if (flow.referenceFlowProperty == null) {
			config.log().error("Could not import flow "
				+ flow.refId + " because the "
				+ "reference flow property of this flow "
				+ "could not be imported.");
			return null;
		}
		return config.db().insert(flow);
	}

	private void createAndMapContent() {
		setFlowType();
		flow.refId = ilcdFlow.getId();
		flow.name = Strings.cut(
			Flows.getFullName(ilcdFlow.flow, ilcdFlow.langs), 2048);
		flow.description = ilcdFlow.getComment();
		flow.casNumber = ilcdFlow.getCasNumber();
		flow.synonyms = ilcdFlow.getSynonyms();
		flow.formula = ilcdFlow.getSumFormula();
		flow.version = Version.fromString(
			ilcdFlow.flow.version).getValue();
		Date time = ilcdFlow.getTimeStamp();
		if (time != null)
			flow.lastChange = time.getTime();
		addFlowProperties();
		mapLocation();
	}

	private void mapLocation() {
		if (ilcdFlow == null || flow == null)
			return;
		String code = config.str(ilcdFlow.getLocation());
		flow.location = Locations.get(code, config);
	}

	private void addFlowProperties() {
		Integer refID = Flows.getReferenceFlowPropertyID(ilcdFlow.flow);
		List<FlowPropertyRef> refs = Flows
			.getFlowProperties(ilcdFlow.flow);
		for (FlowPropertyRef ref : refs) {
			FlowProperty property = importProperty(ref);
			if (property == null)
				continue;
			FlowPropertyFactor factor = new FlowPropertyFactor();
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

	private FlowProperty importProperty(FlowPropertyRef ref) {
		if (ref == null || ref.flowProperty == null)
			return null;
		return FlowPropertyImport.get(config, ref.flowProperty.uuid);
	}

	private void setFlowType() {
		var t = Flows.getType(ilcdFlow.flow);
		if (t == null) {
			flow.flowType = FlowType.ELEMENTARY_FLOW;
			return;
		}
		flow.flowType = switch (t) {
			case WASTE_FLOW -> FlowType.WASTE_FLOW;
			case PRODUCT_FLOW -> FlowType.PRODUCT_FLOW;
			default -> FlowType.ELEMENTARY_FLOW;
		};
	}
}
