package org.openlca.ilcd.util;

import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Location;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Modelling;
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

	public static ProcessInfo getProcessInfo(Process p) {
		if (p == null)
			return null;
		return p.processInfo;
	}

	public static ProcessInfo processInfo(Process p) {
		if (p.processInfo == null)
			p.processInfo = new ProcessInfo();
		return p.processInfo;
	}

	public static DataSetInfo getDataSetInfo(Process p) {
		ProcessInfo pi = getProcessInfo(p);
		if (pi == null)
			return null;
		return pi.dataSetInfo;
	}

	public static DataSetInfo dataSetInfo(Process p) {
		ProcessInfo pi = processInfo(p);
		if (pi.dataSetInfo == null)
			pi.dataSetInfo = new DataSetInfo();
		return pi.dataSetInfo;
	}

	public static ProcessName getProcessName(Process p) {
		DataSetInfo dsi = getDataSetInfo(p);
		if (dsi == null)
			return null;
		return dsi.name;
	}

	public static ProcessName processName(Process p) {
		DataSetInfo dsi = dataSetInfo(p);
		if (dsi.name == null)
			dsi.name = new ProcessName();
		return dsi.name;
	}

	public static Geography getGeography(Process p) {
		ProcessInfo pi = getProcessInfo(p);
		if (pi == null)
			return null;
		return pi.geography;
	}

	public static Geography geography(Process p) {
		ProcessInfo pi = processInfo(p);
		if (pi.geography == null)
			pi.geography = new Geography();
		return pi.geography;
	}

	public static Location getLocation(Process p) {
		Geography g = getGeography(p);
		if (g == null)
			return null;
		return g.location;
	}

	public static Location location(Process p) {
		Geography g = geography(p);
		if (g.location == null)
			g.location = new Location();
		return g.location;
	}

	public static QuantitativeReference getQuantitativeReference(Process p) {
		ProcessInfo pi = getProcessInfo(p);
		if (pi == null)
			return null;
		return pi.quantitativeReference;
	}

	public static QuantitativeReference quantitativeReference(Process p) {
		ProcessInfo pi = processInfo(p);
		if (pi.quantitativeReference == null)
			pi.quantitativeReference = new QuantitativeReference();
		return pi.quantitativeReference;
	}

	public static Technology getTechnology(Process p) {
		ProcessInfo pi = getProcessInfo(p);
		if (pi == null)
			return null;
		return pi.technology;
	}

	public static Technology technology(Process p) {
		ProcessInfo pi = processInfo(p);
		if (pi.technology == null)
			pi.technology = new Technology();
		return pi.technology;
	}

	public static Time getTime(Process p) {
		ProcessInfo pi = getProcessInfo(p);
		if (pi == null)
			return null;
		return pi.time;
	}

	public static Time time(Process p) {
		ProcessInfo pi = processInfo(p);
		if (pi.time == null)
			pi.time = new Time();
		return pi.time;
	}

	public static Modelling getModelling(Process p) {
		if (p == null)
			return null;
		return p.modelling;
	}

	public static Modelling modelling(Process p) {
		if (p.modelling == null)
			p.modelling = new Modelling();
		return p.modelling;
	}

	public static Method getMethod(Process p) {
		Modelling m = getModelling(p);
		if (m == null)
			return null;
		return m.method;
	}

	public static Method method(Process p) {
		Modelling m = modelling(p);
		if (m.method == null)
			m.method = new Method();
		return m.method;
	}

	public static Representativeness getRepresentativeness(Process p) {
		Modelling m = getModelling(p);
		if (m == null)
			return null;
		return m.representativeness;
	}

	public static Representativeness representativeness(Process p) {
		Modelling m = modelling(p);
		if (m.representativeness == null)
			m.representativeness = new Representativeness();
		return m.representativeness;
	}

	public static Validation getValidation(Process p) {
		Modelling m = getModelling(p);
		if (m == null)
			return null;
		return m.validation;
	}

	public static Validation validation(Process p) {
		Modelling m = modelling(p);
		if (m.validation == null)
			m.validation = new Validation();
		return m.validation;
	}

	public static AdminInfo getAdminInfo(Process p) {
		if (p == null)
			return null;
		return p.adminInfo;
	}

	public static AdminInfo adminInfo(Process p) {
		if (p.adminInfo == null)
			p.adminInfo = new AdminInfo();
		return p.adminInfo;
	}

	public static Publication getPublication(Process p) {
		AdminInfo ai = getAdminInfo(p);
		if (ai == null)
			return null;
		return ai.publication;
	}

	public static Publication publication(Process p) {
		AdminInfo ai = adminInfo(p);
		if (ai.publication == null)
			ai.publication = new Publication();
		return ai.publication;
	}

	public static DataEntry getDataEntry(Process p) {
		AdminInfo ai = getAdminInfo(p);
		if (ai == null)
			return null;
		return ai.dataEntry;
	}

	public static DataEntry dataEntry(Process p) {
		AdminInfo ai = adminInfo(p);
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

	public static DataGenerator dataGenerator(Process p) {
		AdminInfo ai = adminInfo(p);
		if (ai.dataGenerator == null)
			ai.dataGenerator = new DataGenerator();
		return ai.dataGenerator;
	}

	public static Exchange exchange(Process p) {
		Exchange e = new Exchange();
		p.exchanges.add(e);
		return e;
	}
}
