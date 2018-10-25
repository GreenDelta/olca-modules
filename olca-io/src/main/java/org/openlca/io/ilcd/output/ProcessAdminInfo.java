package org.openlca.io.ilcd.output;

import java.util.Date;

import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.util.Refs;
import org.openlca.io.Xml;
import org.openlca.util.Strings;

class ProcessAdminInfo {

	private final ExportConfig config;
	private Process process;
	private ProcessDocumentation documentation;
	private AdminInfo iAdminInfo;

	ProcessAdminInfo(ExportConfig config) {
		this.config = config;
	}

	AdminInfo create(Process process) {
		this.process = process;
		this.documentation = process.getDocumentation();
		iAdminInfo = new AdminInfo();
		createDataGenerator();
		createDataEntry();
		createPublication();
		createCommissionerAndGoal();
		return iAdminInfo;
	}

	private void createDataEntry() {
		DataEntry dataEntry = new DataEntry();
		iAdminInfo.dataEntry = dataEntry;
		dataEntry.timeStamp = Xml.calendar(new Date());
		dataEntry.formats.add(Refs.ilcd());
		if (documentation.getDataDocumentor() != null) {
			Ref ref = ExportDispatch.forwardExport(
					documentation.getDataDocumentor(), config);
			if (ref != null) {
				dataEntry.documentor = ref;
			}
		}
	}

	private void createDataGenerator() {
		if (documentation.getDataGenerator() != null) {
			DataGenerator generator = new DataGenerator();
			iAdminInfo.dataGenerator = generator;
			Ref ref = ExportDispatch.forwardExport(
					documentation.getDataGenerator(), config);
			if (ref != null)
				generator.contacts.add(ref);
		}
	}

	private void createPublication() {
		Publication publication = new Publication();
		iAdminInfo.publication = publication;
		if (process.getLastChange() != 0)
			publication.lastRevision = Xml.calendar(process.getLastChange());
		String version = Version.asString(process.getVersion());
		publication.version = version;
		publication.copyright = documentation.isCopyright();
		mapDataSetOwner(publication);
		if (!Strings.nullOrEmpty(documentation.getRestrictions())) {
			publication.accessRestrictions.add(
					LangString.of(documentation.getRestrictions(),
							config.lang));
		}
		mapPublicationSource(publication);
	}

	private void mapDataSetOwner(Publication publication) {
		if (documentation.getDataSetOwner() != null) {
			Ref ref = ExportDispatch.forwardExport(
					documentation.getDataSetOwner(), config);
			if (ref != null) {
				publication.owner = ref;
			}
		}
	}

	private void mapPublicationSource(Publication publication) {
		Source source = documentation.getPublication();
		if (source == null)
			return;
		Ref ref = ExportDispatch
				.forwardExport(source, config);
		if (ref != null)
			publication.republication = ref;
	}

	private void createCommissionerAndGoal() {
		if (Strings.nullOrEmpty(documentation.getIntendedApplication())
				&& Strings.nullOrEmpty(documentation.getProject()))
			return;
		CommissionerAndGoal comAndGoal = new CommissionerAndGoal();
		iAdminInfo.commissionerAndGoal = comAndGoal;
		if (!Strings.nullOrEmpty(documentation.getIntendedApplication())) {
			comAndGoal.intendedApplications.add(
					LangString.of(documentation.getIntendedApplication(),
							config.lang));
		}
		if (!Strings.nullOrEmpty(documentation.getProject())) {
			comAndGoal.project.add(
					LangString.of(documentation.getProject(),
							config.lang));
		}
	}

}
