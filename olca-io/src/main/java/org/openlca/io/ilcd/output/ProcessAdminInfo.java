package org.openlca.io.ilcd.output;

import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.util.Refs;
import org.openlca.io.Xml;
import org.openlca.util.Strings;

import java.util.Date;

class ProcessAdminInfo {

	private final ILCDExport exp;
	private Process process;
	private ProcessDocumentation doc;
	private AdminInfo iAdminInfo;

	ProcessAdminInfo(ILCDExport exp) {
		this.exp = exp;
	}

	AdminInfo create(Process process) {
		this.process = process;
		this.doc = process.documentation;
		iAdminInfo = new AdminInfo();
		if (doc == null)
			return iAdminInfo;
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
		if (doc.dataDocumentor != null) {
			Ref ref = Export.of(doc.dataDocumentor, exp);
			if (ref != null) {
				dataEntry.documentor = ref;
			}
		}
	}

	private void createDataGenerator() {
		if (doc.dataGenerator != null) {
			var generator = new DataGenerator();
			iAdminInfo.dataGenerator = generator;
			Ref ref = Export.of(doc.dataGenerator, exp);
			if (ref != null)
				generator.contacts.add(ref);
		}
	}

	private void createPublication() {
		var pub = new Publication();
		iAdminInfo.publication = pub;
		if (process.lastChange != 0) {
			pub.lastRevision = Xml.calendar(process.lastChange);
		}
		pub.version = Version.asString(process.version);
		pub.copyright = doc.copyright;
		mapDataSetOwner(pub);
		exp.add(pub.accessRestrictions, doc.restrictions);
		mapPublicationSource(pub);
	}

	private void mapDataSetOwner(Publication publication) {
		if (doc.dataSetOwner != null) {
			Ref ref = Export.of(doc.dataSetOwner, exp);
			if (ref != null) {
				publication.owner = ref;
			}
		}
	}

	private void mapPublicationSource(Publication publication) {
		Source source = doc.publication;
		if (source == null)
			return;
		Ref ref = Export.of(source, exp);
		if (ref != null)
			publication.republication = ref;
	}

	private void createCommissionerAndGoal() {
		if (Strings.nullOrEmpty(doc.intendedApplication)
				&& Strings.nullOrEmpty(doc.project))
			return;
		var comAndGoal = new CommissionerAndGoal();
		iAdminInfo.commissionerAndGoal = comAndGoal;
		exp.add(comAndGoal.intendedApplications, doc.intendedApplication);
		exp.add(comAndGoal.project, doc.project);
	}

}
