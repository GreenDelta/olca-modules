package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.ComplianceDeclaration;
import org.openlca.ilcd.processes.ComplianceList;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Location;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.ParameterSection;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.processes.Validation;

public final class Processes {

	private Processes() {
	}

	public static String fullName(Process p, String... langs) {
		var name = getProcessName(p);
		if (name == null)
			return "";
		return Stream.of(
			name.name,
			name.mixAndLocation,
			name.technicalDetails,
			name.flowProperties)
			.map(list -> LangString.getFirst(list, langs))
			.filter(s -> s != null && !s.trim().isEmpty())
			.collect(Collectors.joining(", "));
	}

	public static ProcessInfo getProcessInfo(Process p) {
		if (p == null)
			return null;
		return p.processInfo;
	}

	public static ProcessInfo forceProcessInfo(Process p) {
		if (p.processInfo == null)
			p.processInfo = new ProcessInfo();
		return p.processInfo;
	}

	public static DataSetInfo getDataSetInfo(Process p) {
		var info = getProcessInfo(p);
		if (info == null)
			return null;
		return info.dataSetInfo;
	}

	public static DataSetInfo forceDataSetInfo(Process p) {
		var info = forceProcessInfo(p);
		if (info.dataSetInfo == null)
			info.dataSetInfo = new DataSetInfo();
		return info.dataSetInfo;
	}

	public static ProcessName getProcessName(Process p) {
		var info = getDataSetInfo(p);
		if (info == null)
			return null;
		return info.name;
	}

	public static ProcessName forceProcessName(Process p) {
		var info = forceDataSetInfo(p);
		if (info.name == null)
			info.name = new ProcessName();
		return info.name;
	}

	public static Geography getGeography(Process p) {
		var info = getProcessInfo(p);
		if (info == null)
			return null;
		return info.geography;
	}

	public static Geography forceGeography(Process p) {
		var info = forceProcessInfo(p);
		if (info.geography == null)
			info.geography = new Geography();
		return info.geography;
	}

	public static Location getLocation(Process p) {
		var geo = getGeography(p);
		if (geo == null)
			return null;
		return geo.location;
	}

	public static Location forceLocation(Process p) {
		var geo = forceGeography(p);
		if (geo.location == null)
			geo.location = new Location();
		return geo.location;
	}

	public static QuantitativeReference getQuantitativeReference(Process p) {
		var info = getProcessInfo(p);
		if (info == null)
			return null;
		return info.quantitativeReference;
	}

	public static QuantitativeReference forceQuantitativeReference(Process p) {
		var info = forceProcessInfo(p);
		if (info.quantitativeReference == null)
			info.quantitativeReference = new QuantitativeReference();
		return info.quantitativeReference;
	}

	public static Technology getTechnology(Process p) {
		var info = getProcessInfo(p);
		if (info == null)
			return null;
		return info.technology;
	}

	public static Technology forceTechnology(Process p) {
		var info = forceProcessInfo(p);
		if (info.technology == null)
			info.technology = new Technology();
		return info.technology;
	}

	public static Time getTime(Process p) {
		var info = getProcessInfo(p);
		if (info == null)
			return null;
		return info.time;
	}

	public static Time forceTime(Process p) {
		var info = forceProcessInfo(p);
		if (info.time == null)
			info.time = new Time();
		return info.time;
	}

	public static List<Parameter> getParameters(Process p) {
		var info = getProcessInfo(p);
		if (info == null || info.parameters == null)
			return Collections.emptyList();
		return info.parameters.parameters;
	}

	public static List<Parameter> forceParameters(Process p) {
		var info = forceProcessInfo(p);
		if (info.parameters == null) {
			info.parameters = new ParameterSection();
		}
		return info.parameters.parameters;
	}

	public static Modelling getModelling(Process p) {
		if (p == null)
			return null;
		return p.modelling;
	}

	public static Modelling forceModelling(Process p) {
		if (p.modelling == null)
			p.modelling = new Modelling();
		return p.modelling;
	}

	public static Method getMethod(Process p) {
		var modelling = getModelling(p);
		if (modelling == null)
			return null;
		return modelling.method;
	}

	public static Method forceMethod(Process p) {
		var modelling = forceModelling(p);
		if (modelling.method == null)
			modelling.method = new Method();
		return modelling.method;
	}

	public static Representativeness getRepresentativeness(Process p) {
		var modelling = getModelling(p);
		if (modelling == null)
			return null;
		return modelling.representativeness;
	}

	public static Representativeness forceRepresentativeness(Process p) {
		var modelling = forceModelling(p);
		if (modelling.representativeness == null)
			modelling.representativeness = new Representativeness();
		return modelling.representativeness;
	}

	public static Validation getValidation(Process p) {
		var modelling = getModelling(p);
		if (modelling == null)
			return null;
		return modelling.validation;
	}

	public static Validation forceValidation(Process p) {
		var modelling = forceModelling(p);
		if (modelling.validation == null)
			modelling.validation = new Validation();
		return modelling.validation;
	}

	public static AdminInfo getAdminInfo(Process p) {
		if (p == null)
			return null;
		return p.adminInfo;
	}

	public static AdminInfo forceAdminInfo(Process p) {
		if (p.adminInfo == null) {
			p.adminInfo = new AdminInfo();
		}
		return p.adminInfo;
	}

	public static CommissionerAndGoal getCommissionerAndGoal(Process p) {
		var info = getAdminInfo(p);
		return info == null
				? null
				: info.commissionerAndGoal;
	}

	public static CommissionerAndGoal forceCommissionerAndGoal(Process p) {
		var info = forceAdminInfo(p);
		if (info.commissionerAndGoal == null) {
			info.commissionerAndGoal = new CommissionerAndGoal();
		}
		return info.commissionerAndGoal;
	}

	public static Publication getPublication(Process p) {
		var info = getAdminInfo(p);
		if (info == null)
			return null;
		return info.publication;
	}

	public static Publication forcePublication(Process p) {
		var info = forceAdminInfo(p);
		if (info.publication == null) {
			info.publication = new Publication();
		}
		return info.publication;
	}

	public static DataEntry getDataEntry(Process p) {
		var info = getAdminInfo(p);
		return info == null
				? null
				: info.dataEntry;
	}

	public static DataEntry forceDataEntry(Process p) {
		AdminInfo ai = forceAdminInfo(p);
		if (ai.dataEntry == null)
			ai.dataEntry = new DataEntry();
		return ai.dataEntry;
	}

	public static DataGenerator getDataGenerator(Process p) {
		AdminInfo ai = getAdminInfo(p);
		if (ai == null)
			return null;
		return ai.dataGenerator;
	}

	public static DataGenerator forceDataGenerator(Process p) {
		AdminInfo ai = forceAdminInfo(p);
		if (ai.dataGenerator == null)
			ai.dataGenerator = new DataGenerator();
		return ai.dataGenerator;
	}

	public static Exchange createExchange(Process p) {
		Exchange e = new Exchange();
		p.exchanges.add(e);
		return e;
	}

	public static List<ComplianceDeclaration> getComplianceDeclarations(
			Process p) {
		if (p == null || p.modelling == null
				|| p.modelling.complianceDeclarations == null)
			return Collections.emptyList();
		return p.modelling.complianceDeclarations.entries;
	}

	public static List<ComplianceDeclaration> forceComplianceDeclarations(
			Process p) {
		if (p.modelling == null)
			p.modelling = new Modelling();
		if (p.modelling.complianceDeclarations == null)
			p.modelling.complianceDeclarations = new ComplianceList();
		return p.modelling.complianceDeclarations.entries;
	}

	public static ComplianceDeclaration createComplianceDeclaration(Process p) {
		List<ComplianceDeclaration> list = forceComplianceDeclarations(p);
		ComplianceDeclaration cd = new ComplianceDeclaration();
		list.add(cd);
		return cd;
	}

	public static ComplianceDeclaration getComplianceDeclaration(Process p,
			Ref system) {
		List<ComplianceDeclaration> list = getComplianceDeclarations(p);
		for (ComplianceDeclaration cd : list) {
			if (Objects.equals(cd.system, system))
				return cd;
		}
		return null;
	}
}
