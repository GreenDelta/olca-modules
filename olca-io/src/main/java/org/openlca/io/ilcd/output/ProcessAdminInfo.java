package org.openlca.io.ilcd.output;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.DataSetReference;
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

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ExportConfig config;
	private Process process;
	private ProcessDocumentation documentation;
	private AdministrativeInformation iAdminInfo;

	ProcessAdminInfo(ExportConfig config) {
		this.config = config;
	}

	AdministrativeInformation create(Process process) {
		this.process = process;
		this.documentation = process.getDocumentation();
		iAdminInfo = new AdministrativeInformation();
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
		dataEntry.getReferenceToDataSetFormat().add(
				Reference.forIlcdFormat(config.ilcdConfig));
		if (documentation.getDataDocumentor() != null) {
			DataSetReference ref = ExportDispatch.forwardExportCheck(
					documentation.getDataDocumentor(), config);
			if (ref != null) {
				dataEntry.setReferenceToPersonOrEntityEnteringTheData(ref);
			}
		}
	}

	private void createDataGenerator() {
		if (documentation.getDataGenerator() != null) {
			DataGenerator generator = new DataGenerator();
			iAdminInfo.setDataGenerator(generator);
			DataSetReference ref = ExportDispatch.forwardExportCheck(
					documentation.getDataGenerator(), config);
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
		if (process.getLastChange() != 0)
			publication.setDateOfLastRevision(toXmlCalender(new Date(process
					.getLastChange())));
		String version = Version.asString(process.getVersion());
		publication.setDataSetVersion(version);
		publication.setCopyright(documentation.isCopyright());
		mapDataSetOwner(publication);
		if (!Strings.nullOrEmpty(documentation.getRestrictions())) {
			publication.getAccessRestrictions().add(
					LangString.freeText(documentation.getRestrictions(),
							config.ilcdConfig));
		}
		mapPublicationSource(publication);
	}

	private void mapDataSetOwner(Publication publication) {
		if (documentation.getDataSetOwner() != null) {
			DataSetReference ref = ExportDispatch.forwardExportCheck(
					documentation.getDataSetOwner(), config);
			if (ref != null) {
				publication.setReferenceToOwnershipOfDataSet(ref);
			}
		}
	}

	private void mapPublicationSource(Publication publication) {
		Source source = documentation.getPublication();
		if (source == null)
			return;
		DataSetReference ref = ExportDispatch
				.forwardExportCheck(source, config);
		if (ref != null)
			publication.setReferenceToUnchangedRepublication(ref);
	}

	private void createCommissionerAndGoal() {
		if (Strings.nullOrEmpty(documentation.getIntendedApplication())
				&& Strings.nullOrEmpty(documentation.getProject()))
			return;
		CommissionerAndGoal comAndGoal = new CommissionerAndGoal();
		iAdminInfo.setCommissionerAndGoal(comAndGoal);
		if (!Strings.nullOrEmpty(documentation.getIntendedApplication())) {
			comAndGoal.getIntendedApplications().add(
					LangString.freeText(documentation.getIntendedApplication(),
							config.ilcdConfig));
		}
		if (!Strings.nullOrEmpty(documentation.getProject())) {
			comAndGoal.getProject().add(
					LangString.label(documentation.getProject(),
							config.ilcdConfig));
		}
	}

}
