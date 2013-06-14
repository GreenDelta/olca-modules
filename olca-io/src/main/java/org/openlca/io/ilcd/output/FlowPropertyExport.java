/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/

package org.openlca.io.ilcd.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.flowproperties.DataSetInformation;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.util.FlowPropertyBuilder;
import org.openlca.ilcd.util.LangString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The export of an openLCA flow property to an ILCD flow property data set.
 */
public class FlowPropertyExport {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private FlowProperty flowProperty;
	private IDatabase database;
	private DataStore dataStore;
	private String baseUri;

	public FlowPropertyExport(IDatabase database, DataStore dataStore) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public org.openlca.ilcd.flowproperties.FlowProperty run(
			FlowProperty flowProperty) throws DataStoreException {
		loadProperty(flowProperty);
		DataSetInformation dataSetInfo = makeDataSetInfo();
		DataSetReference unitGroupRef = makeUnitGroupRef();
		org.openlca.ilcd.flowproperties.FlowProperty iProperty = FlowPropertyBuilder
				.makeFlowProperty().withBaseUri(baseUri)
				.withDataSetInfo(dataSetInfo)
				.withUnitGroupReference(unitGroupRef).getFlowProperty();
		dataStore.put(iProperty, flowProperty.getId());
		return iProperty;
	}

	private void loadProperty(FlowProperty property) throws DataStoreException {
		try {
			this.flowProperty = database.createDao(FlowProperty.class)
					.getForId(property.getId());
		} catch (Exception e) {
			throw new DataStoreException(
					"Cannot load flow property from database.", e);
		}
	}

	private DataSetInformation makeDataSetInfo() {
		DataSetInformation dataSetInfo = new DataSetInformation();
		dataSetInfo.setUUID(flowProperty.getId());
		LangString.addLabel(dataSetInfo.getName(), flowProperty.getName());
		if (flowProperty.getDescription() != null) {
			LangString.addFreeText(dataSetInfo.getGeneralComment(),
					flowProperty.getDescription());
		}
		CategoryConverter converter = new CategoryConverter(FlowProperty.class,
				database);
		ClassificationInformation classInfo = converter
				.getClassificationInformation(flowProperty.getCategoryId());
		dataSetInfo.setClassificationInformation(classInfo);
		return dataSetInfo;
	}

	private DataSetReference makeUnitGroupRef() {
		UnitGroup unitGroup = loadUnitGroup();
		return ExportDispatch
				.forwardExportCheck(unitGroup, database, dataStore);
	}

	private UnitGroup loadUnitGroup() {
		String unitGroupId = flowProperty.getUnitGroupId();
		UnitGroup unitGroup = null;
		try {
			unitGroup = database.createDao(UnitGroup.class).getForId(
					unitGroupId);
		} catch (Exception e) {
			log.error("Cannot load unit group for id=" + unitGroupId, e);
		}
		return unitGroup;
	}

}
