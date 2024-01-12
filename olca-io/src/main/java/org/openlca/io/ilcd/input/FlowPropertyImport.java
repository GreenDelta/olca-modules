package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.FlowPropertyBag;

import java.util.Date;

public class FlowPropertyImport {

	private final Import imp;
	private FlowPropertyBag ilcdProperty;
	private FlowProperty property;

	public FlowPropertyImport(Import imp) {
		this.imp = imp;
	}

	public FlowProperty run(
			org.openlca.ilcd.flowproperties.FlowProperty dataSet) {
		this.ilcdProperty = new FlowPropertyBag(dataSet, imp.langOrder());
		var prop = imp.db().get(FlowProperty.class, dataSet.getUUID());
		return prop != null
				? prop
				: createNew();
	}

	public static FlowProperty get(Import imp, String id) {
		var property = imp.db().get(FlowProperty.class, id);
		if (property != null)
			return property;
		var dataSet = imp.store().get(
				org.openlca.ilcd.flowproperties.FlowProperty.class, id);
		if (dataSet == null) {
			imp.log().error("invalid reference in ILCD data set:" +
					" flow property '" + id + "' does not exist");
			return null;
		}
		return new FlowPropertyImport(imp).run(dataSet);
	}

	private FlowProperty createNew() {
		property = new FlowProperty();
		var path = Categories.getPath(ilcdProperty.getValue());
		property.category = new CategoryDao(imp.db())
				.sync(ModelType.FLOW_PROPERTY, path);
		mapDescriptionAttributes();
		Ref unitGroupRef = ilcdProperty.getUnitGroupReference();
		if (unitGroupRef != null) {
			property.unitGroup = UnitGroupImport.get(imp, unitGroupRef.uuid);
		}
		return imp.insert(property);
	}

	private void mapDescriptionAttributes() {
		property.flowPropertyType = FlowPropertyType.PHYSICAL; // default
		property.refId = ilcdProperty.getId();
		property.name = ilcdProperty.getName();
		property.description = ilcdProperty.getComment();

		String v = ilcdProperty.getVersion();
		property.version = Version.fromString(v).getValue();
		Date time = ilcdProperty.getTimeStamp();
		if (time != null) {
			property.lastChange = time.getTime();
		}
	}
}
