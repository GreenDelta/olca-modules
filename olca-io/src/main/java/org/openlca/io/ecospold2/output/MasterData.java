package org.openlca.io.ecospold2.output;

import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.ActivityIndexEntry;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.ElementaryExchange;
import org.openlca.ecospold2.Geography;
import org.openlca.ecospold2.IntermediateExchange;
import org.openlca.ecospold2.TimePeriod;
import org.openlca.ecospold2.UserMasterData;

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
//	private void writeParamters(UserMasterData masterData) {
//		for (Parameter parameter : dataSet.getParameters()) {
//			Parameter masterParam = new Parameter();
//			masterData.getParameters().add(masterParam);
//			masterParam.setId(parameter.getId());
//			masterParam.setName(parameter.getName());
//			masterParam.setUnitName(parameter.getUnitName());
//		}
//	}

	public static void writeElemFlow(ElementaryExchange elemFlow,
			UserMasterData masterData) {
		ElementaryExchange masterFlow = new ElementaryExchange();
		masterData.getElementaryExchanges().add(masterFlow);
		masterFlow.id = elemFlow.elementaryExchangeId;
		masterFlow.name = elemFlow.name;
		masterFlow.unitId = elemFlow.unitId;
		masterFlow.unitName = elemFlow.unitName;
		masterFlow.compartment = elemFlow.compartment;
		masterFlow.casNumber = elemFlow.casNumber;
		masterFlow.formula = elemFlow.formula;
	}

	public static void writeTechFlow(IntermediateExchange techFlow,
			UserMasterData masterData) {
		IntermediateExchange masterFlow = new IntermediateExchange();
		masterData.getIntermediateExchanges().add(masterFlow);
		masterFlow.id = techFlow.intermediateExchangeId; // !
		masterFlow.unitId = techFlow.unitId;
		masterFlow.name = techFlow.name;
		masterFlow.unitName = techFlow.unitName;
	}

	public static void writeIndexEntry(DataSet dataSet) {
		if(dataSet == null || dataSet.getMasterData() == null)
			return;
		ActivityIndexEntry indexEntry = new ActivityIndexEntry();
		dataSet.getMasterData().getActivityIndexEntries().add(indexEntry);
		Activity activity = dataSet.getActivity();
		if (activity != null) {
			indexEntry.setActivityNameId(activity.getActivityNameId());
			indexEntry.setId(activity.getId());
		}
		TimePeriod timePeriod = dataSet.getTimePeriod();
		if (timePeriod != null) {
			indexEntry.setEndDate(timePeriod.getEndDate());
			indexEntry.setStartDate(timePeriod.getStartDate());
		}
		Geography geography = dataSet.getGeography();
		if (geography != null)
			indexEntry.setGeographyId(geography.getId());
		indexEntry.setSystemModelId("8b738ea0-f89e-4627-8679-433616064e82");
	}

}
