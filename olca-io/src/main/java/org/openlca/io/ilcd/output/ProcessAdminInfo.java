package org.openlca.io.ilcd.output;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AdminInfo;
import org.openlca.core.model.Source;
import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.processes.AdministrativeInformation;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.Reference;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessAdminInfo {

	private AdminInfo adminInfo;
	private AdministrativeInformation iAdminInfo;
	private IDatabase database;
	private DataStore dataStore;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public ProcessAdminInfo(AdminInfo adminInfo) {
		this.adminInfo = adminInfo;
	}

	public AdministrativeInformation create(IDatabase database,
			DataStore dataStore) {
		iAdminInfo = new AdministrativeInformation();
		this.database = database;
		this.dataStore = dataStore;
		createDataGenerator();
		createDataEntry();
		createPublication();
		createCommissionerAndGoal();
		return iAdminInfo;
	}

	private void createDataEntry() {
		DataEntry dataEntry = new DataEntry();
		iAdminInfo.setDataEntry(dataEntry);
		dataEntry.setTimeStamp(toXmlCalender(new Date()));
		dataEntry.getReferenceToDataSetFormat().add(Reference.forIlcdFormat());
		if (adminInfo.getDataDocumentor() != null) {
			DataSetReference ref = ExportDispatch.forwardExportCheck(
					adminInfo.getDataDocumentor(), database, dataStore);
			if (ref != null) {
				dataEntry.setReferenceToPersonOrEntityEnteringTheData(ref);
			}
		}
	}

	private void createDataGenerator() {
		if (adminInfo.getDataGenerator() != null) {
			DataGenerator generator = new DataGenerator();
			iAdminInfo.setDataGenerator(generator);
			DataSetReference ref = ExportDispatch.forwardExportCheck(
					adminInfo.getDataGenerator(), database, dataStore);
			if (ref != null)
				generator.getReferenceToPersonOrEntityGeneratingTheDataSet()
						.add(ref);
		}
	}

	private XMLGregorianCalendar toXmlCalender(Date date) {
		Date _date = date == null ? new Date() : date;
		GregorianCalendar gCal = new GregorianCalendar();
		gCal.setTime(_date);
		try {
			XMLGregorianCalendar cal = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gCal);
			return cal;
		} catch (Exception e) {
			log.warn("Cannot create XML Gregorian Calender", e);
			return null;
		}
	}

	private void createPublication() {
		Publication publication = new Publication();
		iAdminInfo.setPublication(publication);
		publication.setDateOfLastRevision(toXmlCalender(adminInfo
				.getLastChange()));
		String version = adminInfo.getVersion() == null ? "01.00.000"
				: adminInfo.getVersion();
		publication.setDataSetVersion(version);
		publication.setCopyright(adminInfo.getCopyright());
		mapDataSetOwner(publication);
		if (!Strings.nullOrEmpty(adminInfo.getAccessAndUseRestrictions())) {
			publication.getAccessRestrictions()
					.add(LangString.freeText(adminInfo
							.getAccessAndUseRestrictions()));
		}
		mapPublicationSource(publication);
	}

	private void mapDataSetOwner(Publication publication) {
		if (adminInfo.getDataSetOwner() != null) {
			DataSetReference ref = ExportDispatch.forwardExportCheck(
					adminInfo.getDataSetOwner(), database, dataStore);
			if (ref != null) {
				publication.setReferenceToOwnershipOfDataSet(ref);
			}
		}
	}

	private void mapPublicationSource(Publication publication) {
		Source source = adminInfo.getPublication();
		if (source == null)
			return;
		DataSetReference ref = ExportDispatch.forwardExportCheck(source,
				database, dataStore);
		if (ref != null)
			publication.setReferenceToUnchangedRepublication(ref);
	}

	private void createCommissionerAndGoal() {
		if (Strings.nullOrEmpty(adminInfo.getIntendedApplication())
				&& Strings.nullOrEmpty(adminInfo.getProject()))
			return;
		CommissionerAndGoal comAndGoal = new CommissionerAndGoal();
		iAdminInfo.setCommissionerAndGoal(comAndGoal);
		if (!Strings.nullOrEmpty(adminInfo.getIntendedApplication())) {
			comAndGoal.getIntendedApplications().add(
					LangString.freeText(adminInfo.getIntendedApplication()));
		}
		if (!Strings.nullOrEmpty(adminInfo.getProject())) {
			comAndGoal.getProject().add(
					LangString.label(adminInfo.getProject()));
		}
	}

}
