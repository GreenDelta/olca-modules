package org.openlca.io.ilcd.input;

import java.util.Objects;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.Flows;
import org.openlca.io.maps.SyncFlow;
import org.openlca.util.Strings;

public class FlowImport {

	private final Import imp;
	private final org.openlca.ilcd.flows.Flow ds;
	private Flow flow;

	public FlowImport(Import imp, org.openlca.ilcd.flows.Flow ds) {
		this.imp = imp;
		this.ds = ds;
	}

	public SyncFlow run() {
		return imp.flowSync.createIfAbsent(Flows.getUUID(ds), this::createNew);
	}

	public static SyncFlow get(Import imp, String id) {
		return imp.flowSync.createIfAbsent(id, () -> {
			var ds = imp.store().get(org.openlca.ilcd.flows.Flow.class, id);
			return ds != null
					? new FlowImport(imp, ds).createNew()
					: null;
		});
	}

	private Flow createNew() {
		flow = new Flow();
		String[] path = Categories.getPath(ds);
		flow.category = new CategoryDao(imp.db())
				.sync(ModelType.FLOW, path);
		createAndMapContent();
		if (flow.referenceFlowProperty == null) {
			imp.log().error("Could not import flow "
					+ flow.refId + " because the "
					+ "reference flow property of this flow "
					+ "could not be imported.");
		}
		return imp.insert(flow);
	}

	private void createAndMapContent() {
		flow.refId = Flows.getUUID(ds);
		flow.name = Strings.cut(
				Flows.getFullName(ds, imp.lang()), 2048);
		Import.mapVersionInfo(ds, flow);
		flow.flowType = flowType();

		var info = Flows.getDataSetInfo(ds);
		if (info != null) {
			flow.description = imp.str(info.getComment());
			flow.casNumber = info.getCasNumber();
			flow.synonyms = imp.str(info.getSynonyms());
			flow.formula = info.getSumFormula();
		}

		var geo = Flows.getGeography(ds);
		if (geo != null) {
			var loc = imp.str(geo.getLocation());
			flow.location = imp.cache.locationOf(loc);
		}

		addFlowProperties();
	}

	private void addFlowProperties() {
		Integer refID = Flows.getReferenceFlowPropertyID(ds);
		boolean addItems = false;
		for (var ref : Flows.getFlowProperties(ds)) {
			if (ref == null
					|| ref.getFlowProperty() == null
					|| ref.getMeanValue() == 0)
				continue;
			var prop = FlowPropertyImport.get(imp, ref.getFlowProperty().getUUID());
			if (prop == null)
				continue;
			flow.property(prop, ref.getMeanValue());
			if (Objects.equals(refID, ref.getDataSetInternalID())) {
				flow.referenceFlowProperty = prop;
				addItems = ref.getMeanValue() != 1;
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
		var type = Flows.getType(ds);
		if (type == null)
			return FlowType.ELEMENTARY_FLOW;
		return switch (type) {
			case ELEMENTARY_FLOW -> FlowType.ELEMENTARY_FLOW;
			case PRODUCT_FLOW, OTHER_FLOW -> FlowType.PRODUCT_FLOW;
			case WASTE_FLOW -> FlowType.WASTE_FLOW;
		};
	}
}
