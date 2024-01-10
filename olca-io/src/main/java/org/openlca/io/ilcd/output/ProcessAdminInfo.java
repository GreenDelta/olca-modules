package org.openlca.io.ilcd.output;

import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDoc;
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

	private final Export exp;
	private Process process;
	private ProcessDoc doc;
	private AdminInfo iAdminInfo;

	ProcessAdminInfo(Export exp) {
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
		var entry = new DataEntry();
		iAdminInfo.dataEntry = entry;
		entry.timeStamp = Xml.calendar(new Date());
		entry.formats.add(Refs.ilcd());
		entry.documentor = exp.writeRef(doc.dataDocumentor);
	}

	private void createDataGenerator() {
		if (doc.dataGenerator != null) {
			var generator = new DataGenerator();
			iAdminInfo.dataGenerator = generator;
			Ref ref = exp.writeRef(doc.dataGenerator);
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
		pub.owner = exp.writeRef(doc.dataOwner);
		exp.add(pub.accessRestrictions, doc.accessRestrictions);
		pub.republication = exp.writeRef(doc.publication);
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
