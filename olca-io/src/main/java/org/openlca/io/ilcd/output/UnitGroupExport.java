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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.units.DataSetInformation;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.UnitExtension;
import org.openlca.ilcd.util.UnitGroupBuilder;

/**
 * The export of an openLCA unit group to an ILCD data set.
 */
public class UnitGroupExport {

	private UnitGroup unitGroup;
	private IDatabase database;
	private DataStore dataStore;
	private String baseUri;

	public UnitGroupExport(IDatabase database, DataStore dataStore) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public org.openlca.ilcd.units.UnitGroup run(UnitGroup unitGroup)
			throws DataStoreException {
		loadUnitGroup(unitGroup);
		DataSetInformation dataSetInfo = makeDataSetInfo();
		List<org.openlca.ilcd.units.Unit> iUnits = makeUnits();
		org.openlca.ilcd.units.UnitGroup iUnitGroup = UnitGroupBuilder
				.makeUnitGroup().withBaseUri(baseUri)
				.withDataSetInfo(dataSetInfo).withReferenceUnitId(0)
				.withUnits(iUnits).getUnitGroup();
		dataStore.put(iUnitGroup, unitGroup.getId());
		return iUnitGroup;
	}

	private void loadUnitGroup(UnitGroup unitGroup) throws DataStoreException {
		try {
			this.unitGroup = database
					.select(UnitGroup.class, unitGroup.getId());
		} catch (Exception e) {
			throw new DataStoreException(
					"Cannot load unit group from database.", e);
		}
	}

	private DataSetInformation makeDataSetInfo() {
		DataSetInformation dataSetInfo = new DataSetInformation();
		dataSetInfo.setUUID(unitGroup.getId());
		LangString.addLabel(dataSetInfo.getName(), unitGroup.getName());
		if (unitGroup.getDescription() != null)
			LangString.addFreeText(dataSetInfo.getGeneralComment(),
					unitGroup.getDescription());
		CategoryConverter converter = new CategoryConverter(UnitGroup.class,
				database);
		ClassificationInformation classInfo = converter
				.getClassificationInformation(unitGroup.getCategoryId());
		dataSetInfo.setClassificationInformation(classInfo);
		return dataSetInfo;
	}

	private List<org.openlca.ilcd.units.Unit> makeUnits() {
		List<org.openlca.ilcd.units.Unit> iUnits = new ArrayList<>();
		Unit refUnit = unitGroup.getReferenceUnit();
		int pos = 1;
		for (Unit unit : unitGroup.getUnits()) {
			org.openlca.ilcd.units.Unit iUnit = makeUnit(unit);
			if (unit.equals(refUnit)) {
				iUnit.setDataSetInternalID(BigInteger.valueOf(0));
			} else {
				iUnit.setDataSetInternalID(BigInteger.valueOf(pos));
				pos++;
			}
			iUnits.add(iUnit);
		}
		return iUnits;
	}

	private org.openlca.ilcd.units.Unit makeUnit(Unit unit) {
		org.openlca.ilcd.units.Unit iUnit = new org.openlca.ilcd.units.Unit();
		iUnit.setMeanValue(unit.getConversionFactor());
		iUnit.setName(unit.getName());
		if (unit.getDescription() != null) {
			LangString.addLabel(iUnit.getGeneralComment(),
					unit.getDescription());
		}
		UnitExtension unitExtension = new UnitExtension(iUnit);
		unitExtension.setUnitId(unit.getId());
		return iUnit;
	}
}
