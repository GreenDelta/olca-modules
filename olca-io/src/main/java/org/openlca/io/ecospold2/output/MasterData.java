package org.openlca.io.ecospold2.output;

import spold2.Activity;
import spold2.ActivityDescription;
import spold2.ActivityIndexEntry;
import spold2.DataSet;
import spold2.ElementaryExchange;
import spold2.Geography;
import spold2.IntermediateExchange;
import spold2.Time;
import spold2.UserMasterData;

/**
 * Adds master data entries to an EcoSpold 02 activity data set. This is not
 * documented in the EcoSpold 02 / EcoEditor specification but can be seen when
 * creating data sets with the EcoEditor. It is possible (and required before
 * opening) to import such master data from an EcoSpold 02 file.
 */
final class MasterData {

	private MasterData() {
	}

	// TODO: handle parameters
	// private void writeParamters(UserMasterData masterData) {
	// for (Parameter parameter : dataSet.getParameters()) {
	// Parameter masterParam = new Parameter();
	// masterData.getParameters().add(masterParam);
	// masterParam.setId(parameter.getId());
	// masterParam.setName(parameter.getName());
	// masterParam.setUnitName(parameter.getUnitName());
	// }
	// }

	public static void writeElemFlow(ElementaryExchange elemFlow,
			UserMasterData masterData) {
		ElementaryExchange masterFlow = new ElementaryExchange();
		masterData.elementaryExchanges.add(masterFlow);
		masterFlow.id = elemFlow.flowId;
		masterFlow.name = elemFlow.name;
		masterFlow.unitId = elemFlow.unitId;
		masterFlow.unit = elemFlow.unit;
		masterFlow.compartment = elemFlow.compartment;
		masterFlow.casNumber = elemFlow.casNumber;
		masterFlow.formula = elemFlow.formula;
	}

	public static void writeTechFlow(IntermediateExchange techFlow,
			UserMasterData masterData) {
		IntermediateExchange masterFlow = new IntermediateExchange();
		masterData.intermediateExchanges.add(masterFlow);
		masterFlow.id = techFlow.flowId; // !
		masterFlow.unitId = techFlow.unitId;
		masterFlow.name = techFlow.name;
		masterFlow.unit = techFlow.unit;
	}

	public static void writeIndexEntry(DataSet ds) {
		if (ds == null || ds.masterData == null)
			return;
		ActivityIndexEntry indexEntry = new ActivityIndexEntry();
		ds.masterData.activityIndexEntries.add(indexEntry);
		indexEntry.systemModelId = "8b738ea0-f89e-4627-8679-433616064e82";
		ActivityDescription d = ds.description;
		if (d == null)
			return;
		Activity activity = d.activity;
		if (activity != null) {
			indexEntry.activityNameId = activity.activityNameId;
			indexEntry.id = activity.id;
		}
		Time timePeriod = d.timePeriod;
		if (timePeriod != null) {
			indexEntry.endDate = timePeriod.end;
			indexEntry.startDate = timePeriod.start;
		}
		Geography geography = d.geography;
		if (geography != null)
			indexEntry.geographyId = geography.id;
	}

}
