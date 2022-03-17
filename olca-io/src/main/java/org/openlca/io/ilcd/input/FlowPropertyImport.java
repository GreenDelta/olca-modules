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

	private final ImportConfig config;
	private FlowPropertyBag ilcdProperty;
	private FlowProperty property;

	public FlowPropertyImport(ImportConfig config) {
		this.config = config;
	}

	public FlowProperty run(
			org.openlca.ilcd.flowproperties.FlowProperty dataSet) {
		this.ilcdProperty = new FlowPropertyBag(dataSet, config.langOrder());
		var prop = config.db().get(FlowProperty.class, dataSet.getUUID());
		return prop != null
			? prop
			: createNew();
	}

	public static FlowProperty get(ImportConfig config, String id) {
		var property = config.db().get(FlowProperty.class, id);
		if (property != null)
			return property;
		var dataSet = config.store().get(
			org.openlca.ilcd.flowproperties.FlowProperty.class, id);
		if (dataSet == null) {
			config.log().error("invalid reference in ILCD data set:" +
				" flow property '" + id + "' does not exist");
			return null;
		}
		return new FlowPropertyImport(config).run(dataSet);
	}

	private FlowProperty createNew() {
		property = new FlowProperty();
		var path = Categories.getPath(ilcdProperty.getValue());
		property.category = new CategoryDao(config.db())
				.sync(ModelType.FLOW_PROPERTY, path);
		mapDescriptionAttributes();
		Ref unitGroupRef = ilcdProperty.getUnitGroupReference();
		if (unitGroupRef != null) {
			property.unitGroup = UnitGroupImport.get(config, unitGroupRef.uuid);
		}
		return config.insert(property);
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
