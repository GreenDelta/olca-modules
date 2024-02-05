package org.openlca.io.ilcd.output;

import org.openlca.core.model.Process;
import org.openlca.core.model.Version;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.util.Refs;
import org.openlca.io.Xml;
import org.openlca.util.Strings;

import java.util.Date;

class ProcessAdminInfo {

	private final Export exp;
	private Process process;
	private ProcessDoc doc;
	private AdminInfo adminInfo;

	ProcessAdminInfo(Export exp) {
		this.exp = exp;
	}

	AdminInfo create(Process process) {
		this.process = process;
		this.doc = process.documentation;
		adminInfo = new AdminInfo();
		if (doc == null)
			return adminInfo;
		createDataGenerator();
		createDataEntry();
		createPublication();
		createCommissionerAndGoal();
		return adminInfo;
	}

	private void createDataEntry() {
		adminInfo.withDataEntry()
				.withTimeStamp(Xml.calendar(new Date()))
				.withDocumentor(exp.writeRef(doc.dataDocumentor))
				.withFormats()
				.add(Refs.ilcd());
	}

	private void createDataGenerator() {
		Ref ref = exp.writeRef(doc.dataGenerator);
		if (ref != null) {
			adminInfo.withDataGenerator()
					.withContacts()
					.add(ref);
		}
	}

	private void createPublication() {
		var pub = adminInfo.withPublication()
				.withVersion(Version.asString(process.version))
				.withCopyright(doc.copyright)
				.withOwner(exp.writeRef(doc.dataOwner))
				.withRepublication(exp.writeRef(doc.publication));
		if (process.lastChange != 0) {
			pub.withLastRevision(Xml.calendar(process.lastChange));
		}
		exp.add(pub::withAccessRestrictions, doc.accessRestrictions);
	}

	private void createCommissionerAndGoal() {
		if (Strings.nullOrEmpty(doc.intendedApplication)
				&& Strings.nullOrEmpty(doc.project))
			return;
		var comAndGoal = adminInfo.withCommissionerAndGoal();
		exp.add(comAndGoal::withIntendedApplications, doc.intendedApplication);
		exp.add(comAndGoal::withProject, doc.project);
	}
}
