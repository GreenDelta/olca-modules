package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.FlowProperties;

public class FlowPropertyImport {

	private final Import imp;
	private final org.openlca.ilcd.flowproperties.FlowProperty ds;
	private FlowProperty prop;

	public FlowPropertyImport(
			Import imp, org.openlca.ilcd.flowproperties.FlowProperty ds) {
		this.imp = imp;
		this.ds = ds;
	}

	public FlowProperty run() {
		var prop = imp.db().get(
				FlowProperty.class, FlowProperties.getUUID(ds));
		return prop != null
				? prop
				: createNew();
	}

	public static FlowProperty get(Import imp, String id) {
		var property = imp.db().get(FlowProperty.class, id);
		return property != null
				? property
				: imp.getFromStore(org.openlca.ilcd.flowproperties.FlowProperty.class, id)
				.map(ds -> new FlowPropertyImport(imp, ds).run())
				.orElse(null);
	}

	private FlowProperty createNew() {
		prop = new FlowProperty();
		prop.category = new CategoryDao(imp.db())
				.sync(ModelType.FLOW_PROPERTY, Categories.getPath(ds));
		mapDescriptionAttributes();
		Import.mapVersionInfo(ds, prop);
		var ref = FlowProperties.getUnitGroupRef(ds);
		if (ref != null) {
			prop.unitGroup = UnitGroupImport.get(imp, ref.getUUID());
		}
		return imp.insert(prop);
	}

	private void mapDescriptionAttributes() {
		prop.flowPropertyType = FlowPropertyType.PHYSICAL; // default
		prop.refId = FlowProperties.getUUID(ds);
		var info = FlowProperties.getDataSetInfo(ds);
		if (info != null) {
			prop.name = imp.str(info.getName());
			prop.description = imp.str(info.getComment());
		}
	}
}
