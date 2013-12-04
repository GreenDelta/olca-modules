package org.openlca.io.ecospold2.input;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.AdministrativeInformation;
import org.openlca.ecospold2.DataEntryBy;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.Geography;
import org.openlca.ecospold2.Representativeness;
import org.openlca.ecospold2.Technology;
import org.openlca.ecospold2.TimePeriod;
import org.openlca.io.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * Maps the process documentation from an EcoSpold 02 data set to an openLCA
 * data set.
 */
class DocImportMapper {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	public DocImportMapper(IDatabase database) {
		this.database = database;
	}

	public void map(DataSet dataSet, Process process) {
		if (dataSet == null || process == null)
			return;
		ProcessDocumentation doc = new ProcessDocumentation();
		process.setDocumentation(doc);
		mapTechnology(dataSet, doc);
		mapGeography(dataSet.getGeography(), process);
		mapTime(dataSet.getTimePeriod(), doc);
		mapAdminInfo(dataSet.getAdministrativeInformation(), doc);
		mapRepresentativeness(dataSet.getRepresentativeness(), doc);
	}

	private void mapRepresentativeness(Representativeness repri,
			ProcessDocumentation doc) {
		if (repri == null)
			return;
		doc.setDataTreatment(repri.getExtrapolations());
		doc.setSampling(repri.getSamplingProcedure());
	}

	private void mapTechnology(DataSet dataSet, ProcessDocumentation doc) {
		Activity activity = dataSet.getActivity();
		Technology technology = dataSet.getTechnology();
		if (activity == null || technology == null)
			return;
		String techText = Joiner
				.on(" ")
				.skipNulls()
				.join(technology.getComment(),
						activity.getIncludedActivitiesStart(),
						activity.getIncludedActivitiesEnd(),
						activity.getAllocationComment());
		doc.setTechnology(techText);
	}

	private void mapGeography(Geography geography, Process process) {
		if (geography == null)
			return;
		process.getDocumentation().setGeography(geography.getComment());
		try {
			String refId = KeyGen.get(geography.getShortName());
			LocationDao dao = new LocationDao(database);
			Location location = dao.getForRefId(refId);
			process.setLocation(location);
		} catch (Exception e) {
			log.error("failed to load geography from DB", e);
		}
	}

	private void mapTime(TimePeriod timePeriod, ProcessDocumentation doc) {
		if (timePeriod == null)
			return;
		doc.setValidFrom(timePeriod.getStartDate());
		doc.setValidUntil(timePeriod.getEndDate());
		doc.setTime(timePeriod.getComment());
	}

	private void mapAdminInfo(AdministrativeInformation adminInfo,
			ProcessDocumentation doc) {
		if (adminInfo == null || adminInfo.getDataEntryBy() == null)
			return;
		DataEntryBy dataEntry = adminInfo.getDataEntryBy();
		ActorDao dao = new ActorDao(database);
		Actor actor = dao.getForRefId(dataEntry.getPersonId());
		if (actor == null) {
			actor = new Actor();
			actor.setRefId(dataEntry.getPersonId());
			actor.setEmail(dataEntry.getPersonEmail());
			actor.setName(dataEntry.getPersonName());
			actor = dao.insert(actor);
		}
		doc.setDataGenerator(actor);
	}

}
