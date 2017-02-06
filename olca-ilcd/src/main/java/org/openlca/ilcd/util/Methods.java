package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.methods.AdminInfo;
import org.openlca.ilcd.methods.DataEntry;
import org.openlca.ilcd.methods.DataSetInfo;
import org.openlca.ilcd.methods.Factor;
import org.openlca.ilcd.methods.FactorList;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.methods.MethodInfo;
import org.openlca.ilcd.methods.Publication;

public final class Methods {

	private Methods() {
	}

	public static MethodInfo getMethodInfo(LCIAMethod m) {
		if (m == null)
			return null;
		return m.methodInfo;
	}

	public static MethodInfo methodInfo(LCIAMethod m) {
		if (m.methodInfo == null)
			m.methodInfo = new MethodInfo();
		return m.methodInfo;
	}

	public static DataSetInfo getDataSetInfo(LCIAMethod m) {
		MethodInfo mi = getMethodInfo(m);
		if (mi == null)
			return null;
		return mi.dataSetInfo;
	}

	public static DataSetInfo dataSetInfo(LCIAMethod m) {
		MethodInfo mi = methodInfo(m);
		if (mi.dataSetInfo == null)
			mi.dataSetInfo = new DataSetInfo();
		return mi.dataSetInfo;
	}

	public static AdminInfo getAdminInfo(LCIAMethod m) {
		if (m == null)
			return null;
		return m.adminInfo;
	}

	public static AdminInfo adminInfo(LCIAMethod m) {
		if (m.adminInfo == null)
			m.adminInfo = new AdminInfo();
		return m.adminInfo;
	}

	public static DataEntry getDataEntry(LCIAMethod m) {
		AdminInfo ai = getAdminInfo(m);
		if (ai == null)
			return null;
		return ai.dataEntry;
	}

	public static DataEntry dataEntry(LCIAMethod m) {
		AdminInfo ai = adminInfo(m);
		if (ai.dataEntry == null)
			ai.dataEntry = new DataEntry();
		return ai.dataEntry;
	}

	public static Publication getPublication(LCIAMethod m) {
		AdminInfo ai = getAdminInfo(m);
		if (ai == null)
			return null;
		return ai.publication;
	}

	public static Publication publication(LCIAMethod m) {
		AdminInfo ai = adminInfo(m);
		if (ai.publication == null)
			ai.publication = new Publication();
		return ai.publication;
	}

	public static List<Factor> getFactors(LCIAMethod m) {
		if (m == null || m.characterisationFactors == null)
			return Collections.emptyList();
		return m.characterisationFactors.factors;
	}

	public static List<Factor> factors(LCIAMethod m) {
		if (m.characterisationFactors == null)
			m.characterisationFactors = new FactorList();
		return m.characterisationFactors.factors;
	}

}
