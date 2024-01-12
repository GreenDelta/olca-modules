package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.FlowBag;
import org.openlca.ilcd.util.Flows;
import org.openlca.io.maps.SyncFlow;
import org.openlca.util.Strings;

import java.util.Objects;

public class FlowImport {

	private final Import imp;
	private FlowBag ilcdFlow;
	private Flow flow;

	public FlowImport(Import imp) {
		this.imp = imp;
	}

	public SyncFlow run(org.openlca.ilcd.flows.Flow dataSet) {
		return imp.flowSync().createIfAbsent(
			dataSet.getUUID(), () -> createNew(dataSet));
	}

	public static SyncFlow get(Import imp, String id) {
		return imp.flowSync().createIfAbsent(id, () -> {
			var dataSet = imp.store().get(org.openlca.ilcd.flows.Flow.class, id);
			if (dataSet == null) {
				return null;
			}
			return new FlowImport(imp).createNew(dataSet);
		});
	}

	private Flow createNew(org.openlca.ilcd.flows.Flow dataSet) {
		this.ilcdFlow = new FlowBag(dataSet, imp.langOrder());
		flow = new Flow();
		String[] path = Categories.getPath(ilcdFlow.flow);
		flow.category = new CategoryDao(imp.db())
			.sync(ModelType.FLOW, path);
		createAndMapContent();
		if (flow.referenceFlowProperty == null) {
			imp.log().error("Could not import flow "
				+ flow.refId + " because the "
				+ "reference flow property of this flow "
				+ "could not be imported.");
			return null;
		}
		return imp.insert(flow);
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
		var locationCode = imp.str(ilcdFlow.getLocation());
		flow.location = imp.cache.locationOf(locationCode);
		addFlowProperties();
	}

	private void addFlowProperties() {
		Integer refID = Flows.getReferenceFlowPropertyID(ilcdFlow.flow);
		boolean addItems = false;
		var refs = Flows.getFlowProperties(ilcdFlow.flow);
		for (FlowPropertyRef ref : refs) {
			if (ref == null || ref.flowProperty == null || ref.meanValue == 0)
				continue;
			var property = FlowPropertyImport.get(imp, ref.flowProperty.uuid);
			if (property == null)
				continue;
			flow.property(property, ref.meanValue);
			if (Objects.equals(refID,  ref.dataSetInternalID)) {
				flow.referenceFlowProperty = property;
				addItems = ref.meanValue != 1;
			}
		}

		// in the ILCD format, specifically for EPD data sets, flows sometimes
		// have a reference flow property factor with a value != 1. In EPDs this
		// is typically used to describe that an item of that product has a
		// specific mass. In this case, we try to add the flow property "number
		// of items" and set this as the reference flow property if possible
		if (!addItems)
			return;
		var num = imp.db().get(
				FlowProperty.class, "01846770-4cfe-4a25-8ad9-919d8d378345");
		if (num == null) {
			imp.log().error("flow " + flow.refId
					+ " has a reference flow property with a factor != 1");
			return;
		}
		for (var f : flow.flowPropertyFactors) {
			if (Objects.equals(f.flowProperty, num)) {
				imp.log().error("flow " + flow.refId
						+ " has a reference flow property with a factor != 1");
				return;
			}
		}
		imp.log().warn("flow " + flow.refId + " has a reference " +
				"flow property with a factor != 1; added 'Number of items' " +
				"as reference flow property");
		flow.property(num, 1);
		flow.referenceFlowProperty = num;
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
