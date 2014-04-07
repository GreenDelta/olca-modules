package org.openlca.io.ecospold2.output;

import org.openlca.core.model.Process;
import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.ActivityIndexEntry;
import org.openlca.ecospold2.ActivityName;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.ElementaryExchange;
import org.openlca.ecospold2.Geography;
import org.openlca.ecospold2.IntermediateExchange;
import org.openlca.ecospold2.Parameter;
import org.openlca.ecospold2.TimePeriod;
import org.openlca.ecospold2.Unit;
import org.openlca.ecospold2.UserMasterData;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds master data entries to an EcoSpold 02 activity data set. This is not
 * documented in the EcoSpold 02 / EcoEditor specification but can be seen when
 * creating data sets with the EcoEditor. It is possible (and required before
 * opening) to import such master data from an EcoSpold 02 file.
 */
class MasterData {

	private Process process;
	private DataSet dataSet;

	private MasterData(Process process, DataSet dataSet) {
		this.process = process;
		this.dataSet = dataSet;
	}

	public static void map(Process process, DataSet dataSet) {
		new MasterData(process, dataSet).map();
	}

	private void map() {
		UserMasterData masterData = new UserMasterData();
		dataSet.setMasterData(masterData);
		writeUnits(masterData);
		ActivityName activityName = new ActivityName();
		masterData.getActivityNames().add(activityName);
		if (dataSet.getActivity() != null)
			activityName.setId(dataSet.getActivity().getActivityNameId());
		activityName.setName(process.getName());
		if (dataSet.getGeography() != null)
			masterData.getGeographies().add(dataSet.getGeography());
		writeParamters(masterData);
		writeElementaryFlows(masterData);
		writeTechFlows(masterData);
		writeIndexEntry(masterData);
	}

	private void writeUnits(UserMasterData masterData) {
		Map<String, String> units = new HashMap<>();
		for (IntermediateExchange exchange : dataSet.getIntermediateExchanges())
			units.put(exchange.getUnitId(), exchange.getUnitName());
		for (ElementaryExchange exchange : dataSet.getElementaryExchanges())
			units.put(exchange.getUnitId(), exchange.getUnitName());
		for (Map.Entry<String, String> entry : units.entrySet()) {
			Unit unit = new Unit();
			unit.setId(entry.getKey());
			unit.setName(entry.getValue());
			masterData.getUnits().add(unit);
		}
	}

	private void writeParamters(UserMasterData masterData) {
		for (Parameter parameter : dataSet.getParameters()) {
			Parameter masterParam = new Parameter();
			masterData.getParameters().add(masterParam);
			masterParam.setId(parameter.getId());
			masterParam.setName(parameter.getName());
			masterParam.setUnitName(parameter.getUnitName());
		}
	}

	private void writeElementaryFlows(UserMasterData masterData) {
		Map<String, ElementaryExchange> exchanges = new HashMap<>();
		for (ElementaryExchange exchange : dataSet.getElementaryExchanges())
			exchanges.put(exchange.getElementaryExchangeId(), exchange);
		for (ElementaryExchange exchange : exchanges.values()) {
			ElementaryExchange masterFlow = new ElementaryExchange();
			masterData.getElementaryExchanges().add(masterFlow);
			masterFlow.setId(exchange.getElementaryExchangeId());
			masterFlow.setName(exchange.getName());
			masterFlow.setUnitId(exchange.getUnitId());
			masterFlow.setUnitName(exchange.getUnitName());
		}
	}

	private void writeTechFlows(UserMasterData masterData) {
		for (IntermediateExchange techFlow : dataSet.getIntermediateExchanges()) {
			IntermediateExchange masterFlow = new IntermediateExchange();
			masterData.getIntermediateExchanges().add(masterFlow);
			masterFlow.setId(techFlow.getIntermediateExchangeId()); // !
			masterFlow.setUnitId(techFlow.getUnitId());
			masterFlow.setName(techFlow.getName());
			masterFlow.setUnitName(techFlow.getUnitName());
		}
	}

	private void writeIndexEntry(UserMasterData masterData) {
		ActivityIndexEntry indexEntry = new ActivityIndexEntry();
		masterData.getActivityIndexEntries().add(indexEntry);
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
